package org.example.plugin.app;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.example.plugin.api.Plugin;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
  public ResponseEntity<Void> enable(@PathVariable String id) {
    try {
      loader.reload(id);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Plugin already loaded");
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown plugin id");
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Load failed");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> disable(@PathVariable String id) {
    try {
      loader.unload(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plugin not loaded");
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unload failed");
    }
  }

  @GetMapping("/ui/{id}/{file}")
  public ResponseEntity<Resource> getUi(@PathVariable String id, @PathVariable String file) {
    ClassLoader cl = loader.getPluginClassLoader(id);
    if (cl == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plugin UI not found");
    }
    Resource resource = new ClassPathResource("ui/" + file, cl);
    if (!resource.exists()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UI resource not found");
    }
    MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
    return ResponseEntity.ok().contentType(mediaType).body(resource);
  }
}