package org.example.plugin.hello;

import org.example.plugin.api.Plugin;

public class HelloPlugin implements Plugin {

  @Override
  public String id() {
    return "hello";
  }

  @Override
  public Object getController() {
    return new HelloController();
  }
}
