package org.example.plugin.main;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;

public class PluginLoader {

  private final Map<String, PluginData> activePlugins = new HashMap<>();
  private final Map<String, File> knownJars = new HashMap<>();

  private static class PluginData {

    Plugin plugin;
    URLClassLoader classLoader;
  }

  public void loadJar(File jarFile) throws Exception {
    URLClassLoader classLoader = new URLClassLoader(
        new URL[]{jarFile.toURI().toURL()},
        Plugin.class.getClassLoader()
    );

    ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
    for (Plugin plugin : serviceLoader) {
      PluginData data = new PluginData();
      data.plugin = plugin;
      data.classLoader = classLoader;
      activePlugins.put(plugin.id(), data);
      knownJars.put(plugin.id(), jarFile);
    }
  }

  public void unload(String id) throws Exception {
    PluginData data = activePlugins.remove(id);
    if (data != null) {
      data.classLoader.close();
    }
  }

  public void reload(String id) throws Exception {
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

  public Optional<Plugin> getById(String id) {
    PluginData data = activePlugins.get(id);
    return data != null ? Optional.of(data.plugin) : Optional.empty();
  }
}
