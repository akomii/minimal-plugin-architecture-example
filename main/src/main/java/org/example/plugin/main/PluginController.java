package org.example.plugin.main;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

  private final PluginLoader loader = new PluginLoader();

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
          }
        }
      }
    }
  }

  @GetMapping
  public List<String> list() {
    return loader.getActive().stream()
        .map(Plugin::id)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}/run")
  public String run(@PathVariable String id, @RequestParam(required = false) String input) {
    return loader.getById(id)
        .map(p -> p.execute(input))
        .orElse("plugin not active: " + id);
  }

  @PostMapping("/{id}/disable")
  public String disable(@PathVariable String id) {
    try {
      loader.unload(id);
      return id + " unloaded from memory";
    } catch (Exception e) {
      return "error unloading plugin";
    }
  }

  @PostMapping("/{id}/enable")
  public String enable(@PathVariable String id) {
    try {
      loader.reload(id);
      return id + " loaded into memory";
    } catch (Exception e) {
      return "error loading plugin";
    }
  }
}
