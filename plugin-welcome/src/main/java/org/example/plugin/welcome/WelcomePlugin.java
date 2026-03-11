package org.example.plugin.welcome;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class WelcomePlugin implements Plugin {

  @Override
  public Object getController() {
    return new WelcomeController();
  }
}
