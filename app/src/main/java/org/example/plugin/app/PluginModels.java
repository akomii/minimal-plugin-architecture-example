package org.example.plugin.app;


import java.io.File;
import java.net.URLClassLoader;
import java.util.List;
import org.example.plugin.api.Plugin;

public class PluginModels {

  public record DiscoveredPluginJar(String id, File file, String version, List<String> dependencies) {

  }

  public record PluginData(Plugin plugin, URLClassLoader classLoader) {

  }

  public record PluginInfoDTO(String id, String version, boolean loaded, boolean inFolder, List<String> dependencies) {

  }
}
