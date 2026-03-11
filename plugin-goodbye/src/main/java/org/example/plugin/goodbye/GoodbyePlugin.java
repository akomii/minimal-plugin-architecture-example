package org.example.plugin.goodbye;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class GoodbyePlugin implements Plugin {

  @Override
  public Object getController() {
    return new GoodbyeController();
  }
}
