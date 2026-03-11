package org.example.plugin.app;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.plugin.app.MetadataExtractor.PomInfo;
import org.example.plugin.app.PluginModels.DiscoveredPluginJar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PluginScanner {

  private static final Logger log = LoggerFactory.getLogger(PluginScanner.class);

  private final MetadataExtractor extractor;
  private final String pluginDir;

  public PluginScanner(MetadataExtractor extractor, @Value("${plugin.dir:plugins}") String pluginDir) {
    this.extractor = extractor;
    this.pluginDir = pluginDir;
  }

  public Map<String, DiscoveredPluginJar> scan(Map<String, DiscoveredPluginJar> knownJars) {
    Map<String, DiscoveredPluginJar> newJar = new HashMap<>();
    File dir = new File(pluginDir);
    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
      if (files != null) {
        for (File file : files) {
          boolean alreadyKnown = knownJars.values().stream().anyMatch(dj -> dj.file().equals(file));
          if (!alreadyKnown) {
            try {
              DiscoveredPluginJar jar = discoverPluginJar(file);
              if (jar != null) {
                newJar.put(jar.id(), jar);
              }
            } catch (Exception e) {
              log.error("Failed to scan {}", file.getName(), e);
            }
          }
        }
      }
    }
    return newJar;
  }

  private DiscoveredPluginJar discoverPluginJar(File jarFile) {
    if (!extractor.isPluginJar(jarFile)) {
      return null;
    }
    PomInfo pomInfo = extractor.extractPomInfo(jarFile);
    if ("unknown".equals(pomInfo.artifactId())) {
      log.warn("Skipping {}, no valid artifactId found", jarFile.getName());
      return null;
    }
    List<String> dependencies = extractor.extractDependencies(jarFile);
    return new DiscoveredPluginJar(pomInfo.artifactId(), jarFile, pomInfo.version(), dependencies);
  }
}