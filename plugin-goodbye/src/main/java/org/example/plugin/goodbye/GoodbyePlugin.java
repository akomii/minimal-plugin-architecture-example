package org.example.plugin.goodbye;

import org.example.plugin.api.Plugin;

public class GoodbyePlugin implements Plugin {

  @Override
  public Object getController() {
    return new GoodbyeController();
  }
}
