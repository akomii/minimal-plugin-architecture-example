package org.example.plugin.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.example.plugin.api.Plugin;

public final class PluginLoader {

  private final ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);

  public List<Plugin> loadAll() {
    List<Plugin> list = new ArrayList<>();
    loader.iterator().forEachRemaining(list::add);
    return list;
  }

  public Optional<Plugin> findById(String id) {
    return loadAll().stream().filter(p -> p.id().equals(id)).findFirst();
  }
}
