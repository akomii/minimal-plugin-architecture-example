package org.example.plugin.three;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class ThreePlugin implements Plugin {

  @Override
  public Object getController() {
    return new ThreeController();
  }
}
