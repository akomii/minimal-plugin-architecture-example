package org.example.plugin.goodbye;

import org.example.plugin.api.Plugin;

public class GoodbyePlugin implements Plugin {

  @Override
  public String id() {
    return "goodbye";
  }

  @Override
  public String execute(String input) {
    return "Goodbye " + (input == null ? "world" : input) + "!";
  }
}
