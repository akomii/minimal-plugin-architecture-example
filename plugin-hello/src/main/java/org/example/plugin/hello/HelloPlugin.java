package org.example.plugin.hello;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class HelloPlugin implements Plugin {

  @Override
  public Object getController() {
    return new HelloController();
  }
}
