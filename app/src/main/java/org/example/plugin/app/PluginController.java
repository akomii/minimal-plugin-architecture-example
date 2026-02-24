package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

  private final PluginLoader loader;

  public PluginController(PluginLoader loader) {
    this.loader = loader;
  }

  @PostConstruct
  public void init() {
    File dir = new File("plugins");
    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
      if (files != null) {
        for (File file : files) {
          try {
            loader.loadJar(file);
          } catch (Exception e) {
            System.err.println("Failed to load " + file.getName());
            e.printStackTrace();
          }
        }
      }
    }
  }

  @GetMapping
  public List<String> list() {
    return loader.getActive().stream().map(Plugin::id).collect(Collectors.toList());
  }

  @PostMapping("/{id}/disable")
  public String disable(@PathVariable String id) {
    try {
      loader.unload(id);
      return id + " unloaded";
    } catch (Exception e) {
      return "error unloading";
    }
  }

  @PostMapping("/{id}/enable")
  public String enable(@PathVariable String id) {
    try {
      loader.reload(id);
      return id + " loaded";
    } catch (Exception e) {
      return "error loading";
    }
  }
}
