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

// TODO plugins with frontend frameworks
// TODO versioned plugins
// TODO plugin with dependencies to other plugins

@Service
public class PluginLoader {

  private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, File> knownJars = new HashMap<>();

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

  public record PluginInfo(String id, boolean loaded, boolean inFolder) {

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
          if (!knownJars.containsValue(file)) {
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
        knownJars.put(plugin.id(), jarFile);
      }
    }
  }

  public void load(String id) throws Exception {
    if (activePlugins.containsKey(id)) {
      throw new IllegalStateException("Plugin already loaded: " + id);
    }
    File jarFile = knownJars.get(id);
    if (jarFile == null || !jarFile.exists()) {
      throw new IllegalArgumentException("Unknown plugin id: " + id);
    }
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
    String beanName = "pluginController_" + pluginId;
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
    String beanName = "pluginController_" + pluginId;
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

  public List<PluginInfo> getPluginStatus() {
    return knownJars.entrySet().stream()
        .map(entry -> new PluginInfo(
            entry.getKey(),
            activePlugins.containsKey(entry.getKey()),
            entry.getValue() != null && entry.getValue().exists()
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
