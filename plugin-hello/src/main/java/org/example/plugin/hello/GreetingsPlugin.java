package org.example.plugin.hello;

import org.example.plugin.api.Plugin;

public class GreetingsPlugin implements Plugin {

  @Override
  public String id() {
    return "hello";
  }

  @Override
  public String greet(String name) {
    return "Hello, " + name + "!";
  }
}
