package org.example.plugin.aloha;

import org.example.plugin.aloha.AlohaController;
import org.example.plugin.api.Plugin;

public class AlohaPlugin implements Plugin {

  @Override
  public Object getController() {
    return new AlohaController();
  }
}
