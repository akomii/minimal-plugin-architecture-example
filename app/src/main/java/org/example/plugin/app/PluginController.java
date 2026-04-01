package org.example.plugin.app;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.example.plugin.app.PluginModels.FrontendPluginDTO;
import org.example.plugin.app.PluginModels.PluginInfoDTO;
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
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

  private final PluginLoader loader;
  private final JsLibraryPluginService libraryService;

  public PluginController(PluginLoader loader, JsLibraryPluginService frontendPluginLibraryService) {
    this.loader = loader;
    this.libraryService = frontendPluginLibraryService;
  }

  @GetMapping
  public ResponseEntity<List<PluginInfoDTO>> listPlugins() {
    return ResponseEntity.ok(loader.getPluginStatus());
  }

  @GetMapping("/frontend")
  public ResponseEntity<List<FrontendPluginDTO>> listFrontendPlugins() {
    return ResponseEntity.ok(libraryService.listFrontendLibraries());
  }

  @PostMapping("/{id}")
  public ResponseEntity<Void> enable(@PathVariable String id) {
    try {
      loader.load(id);
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

  @GetMapping("/ui/{id}/**")
  public ResponseEntity<Resource> getUi(@PathVariable String id, HttpServletRequest request) {
    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String prefix = "/api/plugins/ui/" + id + "/";
    String file = path.substring(path.indexOf(prefix) + prefix.length());
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
