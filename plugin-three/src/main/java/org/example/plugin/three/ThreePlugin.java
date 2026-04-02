package org.example.plugin.three;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

/**
 * Entry point for the third plugin. Registers the backend controller to be discovered by the host application.
 */
@AutoService(Plugin.class)
public class ThreePlugin implements Plugin {

  @Override
  public Object getController() {
    return new ThreeController();
  }
}
