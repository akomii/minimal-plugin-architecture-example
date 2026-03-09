package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.example.plugin.app.MetadataExtractor.PomInfo;
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

@Service
public class PluginLoader {

  //TODO load deps automaticall on startup
  //TODO unload dependend plugins too on unload of parent

  private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

  private static final String BEAN_PREFIX = "pluginController_";

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, DiscoveredJar> knownJars = new HashMap<>();

  private final ApplicationContext context;
  private final RequestMappingHandlerMapping handlerMapping;
  private final String pluginDir;
  private final MetadataExtractor extractor;

  public PluginLoader(
      ApplicationContext context,
      RequestMappingHandlerMapping handlerMapping,
      MetadataExtractor extractor,
      @Value("${plugin.dir:plugins}") String pluginDir) {
    this.context = context;
    this.handlerMapping = handlerMapping;
    this.extractor = extractor;
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
      if (!activePlugins.containsKey(pluginId)) {
        try {
          load(pluginId);
        } catch (Exception e) {
          log.error("Failed to load plugin {}", pluginId, e);
        }
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

  private void discoverJar(File jarFile) {
    PomInfo pomInfo = extractor.extractPomInfo(jarFile);
    if ("unknown".equals(pomInfo.artifactId())) {
      log.warn("Skipping {}, no valid artifactId found", jarFile.getName());
      return;
    }
    List<String> dependencies = extractor.extractDependencies(jarFile);
    knownJars.put(pomInfo.artifactId(), new DiscoveredJar(jarFile, pomInfo.version(), dependencies));
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
        load(dep);
      }
    }
    File jarFile = discoveredJar.file();
    URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Plugin.class.getClassLoader());
    ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
    for (Plugin plugin : serviceLoader) {
      registerPlugin(id, plugin, classLoader);
      break;
    }
  }

  private void registerPlugin(String id, Plugin plugin, URLClassLoader classLoader) throws Exception {
    PluginData data = new PluginData(plugin, classLoader);
    activePlugins.put(id, data);
    Object controller = plugin.getController();
    if (controller != null) {
      registerControllerBean(id, controller);
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
