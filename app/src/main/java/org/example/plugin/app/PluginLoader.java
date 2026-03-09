package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.example.plugin.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
public class PluginLoader {

  private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

  private static final String META_INF_MAVEN = "META-INF/maven/";
  private static final String POM_PROPERTIES = "pom.properties";
  private static final String POM_XML = "pom.xml";
  private static final String PLUGIN_GROUP_ID = "org.example.plugin";
  private static final String API_ARTIFACT_ID = "api";
  private static final String BEAN_PREFIX = "pluginController_";

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, DiscoveredJar> knownJars = new HashMap<>();

  private final ApplicationContext context;
  private final RequestMappingHandlerMapping handlerMapping;
  private final String pluginDir;

  public PluginLoader(ApplicationContext context, RequestMappingHandlerMapping handlerMapping, @Value("${plugin.dir:plugins}") String pluginDir) {
    this.context = context;
    this.handlerMapping = handlerMapping;
    this.pluginDir = pluginDir;
  }

  private record PluginData(Plugin plugin, URLClassLoader classLoader) {

  }

  private record DiscoveredJar(File file, String version, List<String> dependencies) {

  }

  public record PluginInfoDTO(String id, String version, boolean loaded, boolean inFolder, List<String> dependencies) {

  }

  @PostConstruct
  public void init() {
    scanForPlugins();
    for (String pluginId : List.copyOf(knownJars.keySet())) {
      try {
        load(pluginId);
      } catch (Exception e) {
        log.error("Failed to load plugin {}", pluginId, e);
      }
    }
  }

  public void scanForPlugins() {
    File dir = new File(pluginDir);
    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
      if (files != null) {
        for (File file : files) {
          boolean alreadyKnown = knownJars.values().stream().anyMatch(dj -> dj.file().equals(file));
          if (!alreadyKnown) {
            try {
              discoverJar(file);
            } catch (Exception e) {
              log.error("Failed to scan {}", file.getName(), e);
            }
          }
        }
      }
    }
  }

  private void discoverJar(File jarFile) throws Exception {
    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Plugin.class.getClassLoader())) {
      ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
      for (Plugin plugin : serviceLoader) {
        String version = extractJarVersion(jarFile);
        List<String> dependencies = extractDependencies(jarFile);
        knownJars.put(plugin.id(), new DiscoveredJar(jarFile, version, dependencies));
      }
    }
  }

  private String extractJarVersion(File jarFile) {
    try (JarFile jar = new JarFile(jarFile)) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().startsWith(META_INF_MAVEN) && entry.getName().endsWith(POM_PROPERTIES)) {
          Properties props = new Properties();
          try (InputStream is = jar.getInputStream(entry)) {
            props.load(is);
            return props.getProperty("version", "unknown");
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not extract version from {}", jarFile.getName(), e);
    }
    return "unknown";
  }

  private List<String> extractDependencies(File jarFile) {
    List<String> deps = new ArrayList<>();
    try (JarFile jar = new JarFile(jarFile)) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().startsWith(META_INF_MAVEN) && entry.getName().endsWith(POM_XML)) {
          try (InputStream is = jar.getInputStream(entry)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList depNodes = doc.getElementsByTagName("dependency");
            for (int i = 0; i < depNodes.getLength(); i++) {
              Element dep = (Element) depNodes.item(i);
              String groupId = dep.getElementsByTagName("groupId").item(0).getTextContent();
              String artifactId = dep.getElementsByTagName("artifactId").item(0).getTextContent();
              if (PLUGIN_GROUP_ID.equals(groupId) && !API_ARTIFACT_ID.equals(artifactId)) {
                deps.add(artifactId);
              }
            }
            return deps;
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not extract dependencies from {}", jarFile.getName(), e);
    }
    return deps;
  }

  public void load(String id) throws Exception {
    if (activePlugins.containsKey(id)) {
      throw new IllegalStateException("Plugin already loaded: " + id);
    }
    DiscoveredJar discoveredJar = knownJars.get(id);
    if (discoveredJar == null || !discoveredJar.file().exists()) {
      throw new IllegalArgumentException("Unknown plugin id: " + id);
    }
    for (String dep : discoveredJar.dependencies()) {
      if (!activePlugins.containsKey(dep)) {
        throw new RuntimeException("Dependency not loaded: " + dep);
      }
    }
    File jarFile = discoveredJar.file();
    URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Plugin.class.getClassLoader());
    ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
    for (Plugin plugin : serviceLoader) {
      if (plugin.id().equals(id)) {
        registerPlugin(plugin, classLoader);
      }
    }
  }

  private void registerPlugin(Plugin plugin, URLClassLoader classLoader) throws Exception {
    PluginData data = new PluginData(plugin, classLoader);
    activePlugins.put(plugin.id(), data);
    Object controller = plugin.getController();
    if (controller != null) {
      registerControllerBean(plugin.id(), controller);
    }
  }

  private void registerControllerBean(String pluginId, Object controller) throws Exception {
    String beanName = BEAN_PREFIX + pluginId;
    getBeanFactory().registerSingleton(beanName, controller);
    Method detectMethod = AbstractHandlerMethodMapping.class.getDeclaredMethod("detectHandlerMethods", Object.class);
    detectMethod.setAccessible(true);
    detectMethod.invoke(handlerMapping, beanName);
  }

  public void unload(String id) throws Exception {
    PluginData data = activePlugins.remove(id);
    if (data == null) {
      return;
    }
    if (data.plugin().getController() != null) {
      unregisterControllerBean(id);
    }
    data.classLoader().close();
  }

  private void unregisterControllerBean(String pluginId) {
    String beanName = BEAN_PREFIX + pluginId;
    Object controller = context.getBean(beanName);
    List<RequestMappingInfo> toRemove = handlerMapping.getHandlerMethods()
        .entrySet()
        .stream()
        .filter(e -> beanName.equals(e.getValue().getBean()) || controller.equals(e.getValue().getBean()))
        .map(Map.Entry::getKey).toList();
    for (RequestMappingInfo info : toRemove) {
      handlerMapping.unregisterMapping(info);
    }
    getBeanFactory().destroySingleton(beanName);
  }

  public List<PluginInfoDTO> getPluginStatus() {
    return knownJars.entrySet().stream()
        .map(entry -> new PluginInfoDTO(
            entry.getKey(),
            entry.getValue().version(),
            activePlugins.containsKey(entry.getKey()),
            entry.getValue().file() != null && entry.getValue().file().exists(),
            entry.getValue().dependencies()
        ))
        .collect(Collectors.toList());
  }

  public ClassLoader getPluginClassLoader(String id) {
    PluginData data = activePlugins.get(id);
    return data != null ? data.classLoader() : null;
  }

  private DefaultListableBeanFactory getBeanFactory() {
    return (DefaultListableBeanFactory) ((ConfigurableApplicationContext) context).getBeanFactory();
  }
}
