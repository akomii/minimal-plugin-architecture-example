package org.example.plugin.welcome;

import org.example.plugin.api.Plugin;

public class WelcomePlugin implements Plugin {

  @Override
  public String id() {
    return "welcome";
  }

  @Override
  public Object getController() {
    return new WelcomeController();
  }
}
