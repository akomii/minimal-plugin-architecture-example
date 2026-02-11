package org.example.plugin.main;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.example.plugin.api.Plugin;

public final class PluginLoader {

  public List<Plugin> loadFromDirectory(Path pluginsDir) {
    if (!Files.isDirectory(pluginsDir)) {
      return List.of();
    }

    try {
      List<Plugin> result = new ArrayList<>();
      try (var jars = Files.list(pluginsDir).filter(p -> p.toString().endsWith(".jar"))) {
        for (Path jar : jars.toList()) {
          URL url = jar.toUri().toURL();
          ClassLoader cl = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
          ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, cl);
          sl.iterator().forEachRemaining(result::add);
        }
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load plugins from " + pluginsDir, e);
    }
  }
}
