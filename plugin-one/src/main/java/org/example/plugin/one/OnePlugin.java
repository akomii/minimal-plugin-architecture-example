package org.example.plugin.one;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

/**
 * Entry point for the first backend plugin. Uses the AutoService annotation for automatic discovery and provides the OneController instance to the host.
 */
@AutoService(Plugin.class)
public class OnePlugin implements Plugin {

  @Override
  public Object getController() {
    return new OneController();
  }
}
