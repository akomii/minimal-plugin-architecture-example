package org.example.plugin.app;

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
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class PluginLoader {

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, File> knownJars = new HashMap<>();

  private final ApplicationContext context;
  private final RequestMappingHandlerMapping handlerMapping;

  public PluginLoader(ApplicationContext context, RequestMappingHandlerMapping handlerMapping) {
    this.context = context;
    this.handlerMapping = handlerMapping;
  }

  private static class PluginData {

    Plugin plugin;
    URLClassLoader classLoader;
  }

  public void loadJar(File jarFile) throws Exception {
    URLClassLoader classLoader = new URLClassLoader(
        new URL[]{jarFile.toURI().toURL()},
        Plugin.class.getClassLoader()
    );
    registerConfigurationsFromJar(jarFile, classLoader);
    ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
    for (Plugin plugin : serviceLoader) {
      PluginData data = new PluginData();
      data.plugin = plugin;
      data.classLoader = classLoader;
      activePlugins.put(plugin.id(), data);
      knownJars.put(plugin.id(), jarFile);
      Object controller = plugin.getController();
      if (controller != null) {
        String beanName = "pluginController_" + plugin.id();
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableContext.getBeanFactory();
        beanFactory.registerSingleton(beanName, controller);
        Method detectMethod = AbstractHandlerMethodMapping.class.getDeclaredMethod("detectHandlerMethods", Object.class);
        detectMethod.setAccessible(true);
        detectMethod.invoke(handlerMapping, beanName);
      }
    }
  }

  private void registerConfigurationsFromJar(File jar, URLClassLoader cl) throws Exception {
    try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jar)) {
      ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;
      DefaultListableBeanFactory bf = (DefaultListableBeanFactory) ctx.getBeanFactory();
      var entries = jf.entries();
      while (entries.hasMoreElements()) {
        var e = entries.nextElement();
        if (!e.getName().endsWith(".class")) {
          continue;
        }
        if (e.getName().startsWith("org/springframework")) {
          continue;
        }
        if (e.getName().startsWith("META-INF")) {
          continue;
        }
        String className = e.getName()
            .replace('/', '.')
            .replace(".class", "");
        Class<?> clazz;
        try {
          clazz = Class.forName(className, false, cl);
        } catch (Throwable ex) {
          continue;
        }
        if (clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class)) {
          Object instance = clazz.getDeclaredConstructor().newInstance();
          String beanName = "pluginConfig_" + clazz.getName();
          if (!bf.containsSingleton(beanName)) {
            bf.registerSingleton(beanName, instance);
          }
        }
      }
    }
  }

  public void unload(String id) throws Exception {
    PluginData data = activePlugins.remove(id);
    if (data != null) {
      if (data.plugin.getController() != null) {
        String beanName = "pluginController_" + id;
        Object controller = context.getBean(beanName);
        List<RequestMappingInfo> toRemove = handlerMapping.getHandlerMethods().entrySet().stream()
            .filter(e -> beanName.equals(e.getValue().getBean()) || controller.equals(e.getValue().getBean()))
            .map(Map.Entry::getKey)
            .toList();
        for (RequestMappingInfo info : toRemove) {
          handlerMapping.unregisterMapping(info);
        }
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableContext.getBeanFactory();
        beanFactory.destroySingleton(beanName);
      }
      data.classLoader.close();
    }
  }

  public void reload(String id) throws Exception {
    if (activePlugins.containsKey(id)) {
      throw new IllegalStateException("Plugin already loaded: " + id);
    }
    File jarFile = knownJars.get(id);
    if (jarFile != null) {
      loadJar(jarFile);
    } else {
      throw new RuntimeException("Unknown plugin id: " + id);
    }
  }

  public List<Plugin> getActive() {
    return activePlugins.values().stream().map(d -> d.plugin).collect(Collectors.toList());
  }
}
