package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  public ResponseEntity<List<String>> list() {
    return ResponseEntity.ok(
        loader.getActive()
            .stream()
            .map(Plugin::id)
            .collect(Collectors.toList())
    );
  }

  @PostMapping("/{id}")
  public ResponseEntity<?> enable(@PathVariable String id) {
    try {
      loader.reload(id);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unknown plugin id");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Load failed");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> disable(@PathVariable String id) {
    try {
      loader.unload(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plugin not loaded");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unload failed");
    }
  }
}
