package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.example.plugin.app.PluginModels.DiscoveredPluginJar;
import org.example.plugin.app.PluginModels.PluginData;
import org.example.plugin.app.PluginModels.PluginInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PluginLoader {

  private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, DiscoveredPluginJar> knownJars = new HashMap<>();

  private final PluginScanner scanner;
  private final PluginRegistrar registry;

  public PluginLoader(PluginScanner scanner, PluginRegistrar registry) {
    this.scanner = scanner;
    this.registry = registry;
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
    Map<String, DiscoveredPluginJar> newJars = scanner.scan(knownJars);
    knownJars.putAll(newJars);
  }

  public void load(String id) throws Exception {
    if (activePlugins.containsKey(id)) {
      throw new IllegalStateException("Plugin already loaded: " + id);
    }
    DiscoveredPluginJar discoveredJar = knownJars.get(id);
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
      registry.register(id, controller);
    }
  }

  public void unload(String id) throws Exception {
    List<String> dependents = new ArrayList<>();
    for (String activeId : activePlugins.keySet()) {
      DiscoveredPluginJar jar = knownJars.get(activeId);
      if (jar != null && jar.dependencies().contains(id)) {
        dependents.add(activeId);
      }
    }
    for (String dependentId : dependents) {
      unload(dependentId);
    }
    PluginData data = activePlugins.remove(id);
    if (data == null) {
      return;
    }
    if (data.plugin().getController() != null) {
      registry.unregister(id);
    }
    data.classLoader().close();
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
}