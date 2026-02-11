package org.example.plugin.main;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import org.example.plugin.api.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PluginController {

  private final List<Plugin> plugins;

  public PluginController() {
    this.plugins = new PluginLoader().loadFromDirectory(Path.of("plugins"));
  }

  @GetMapping("/plugins")
  public List<String> list() {
    return plugins.stream().map(Plugin::id).toList();
  }

  @GetMapping("/greet/{pluginId}")
  public String greet(@PathVariable String pluginId, @RequestParam(defaultValue = "World") String name) {
    return plugins.stream()
        .filter(p -> p.id().equals(pluginId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No plugin: " + pluginId))
        .greet(name);
  }
}

