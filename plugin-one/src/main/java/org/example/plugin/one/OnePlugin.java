package org.example.plugin.one;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class OnePlugin implements Plugin {

  @Override
  public Object getController() {
    return new OneController();
  }
}
