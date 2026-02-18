package org.example.plugin.main;

import java.util.List;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

  private final PluginLoader loader = new PluginLoader();

  @GetMapping
  public List<String> list() {
    return loader.loadAll().stream()
        .map(Plugin::id)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}/run")
  public String run(@PathVariable String id, @RequestParam(required = false) String input) {
    return loader.findById(id)
        .map(p -> p.execute(input))
        .orElse("plugin not found: " + id);
  }
}
