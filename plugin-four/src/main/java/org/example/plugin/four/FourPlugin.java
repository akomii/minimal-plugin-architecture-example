package org.example.plugin.four;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

/**
 * Entry point for the fourth plugin. Supplies the controller for the module that serves as the UI host.
 */
@AutoService(Plugin.class)
public class FourPlugin implements Plugin {

  @Override
  public Object getController() {
    return new FourController();
  }
}
