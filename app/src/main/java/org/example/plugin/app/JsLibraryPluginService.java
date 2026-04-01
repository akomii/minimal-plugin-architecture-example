package org.example.plugin.app;

import java.util.List;
import java.util.Objects;
import org.example.plugin.app.PluginModels.FrontendPluginDTO;
import org.example.plugin.app.PluginModels.PluginInfoDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class JsLibraryPluginService {

  private final PluginLoader pluginLoader;

  public JsLibraryPluginService(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  public List<FrontendPluginDTO> listFrontendLibraries() {
    return pluginLoader.getPluginStatus().stream()
        .filter(PluginInfoDTO::loaded)
        .map(this::toFrontendPluginDto)
        .filter(Objects::nonNull)
        .toList();
  }

  private FrontendPluginDTO toFrontendPluginDto(PluginInfoDTO plugin) {
    String id = plugin.id();
    ClassLoader classLoader = pluginLoader.getPluginClassLoader(id);
    if (classLoader == null) {
      return null;
    }
    String libraryFile = id + ".js";
    Resource resource = new ClassPathResource("ui/" + libraryFile, classLoader);
    if (!resource.exists()) {
      return null;
    }
    return new FrontendPluginDTO(id, "/api/plugins/ui/" + id + "/" + libraryFile);
  }
}
