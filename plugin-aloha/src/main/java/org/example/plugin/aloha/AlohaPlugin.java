package org.example.plugin.aloha;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class AlohaPlugin implements Plugin {

  @Override
  public Object getController() {
    return new AlohaController();
  }
}
