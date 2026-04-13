package org.example.plugin.two;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

/**
 * Entry point for the second plugin. Supplies the backend controller to the host system for a module that also bundles a frontend Vue application.
 */
@AutoService(Plugin.class)
public class TwoPlugin implements Plugin {

  @Override
  public Object getController() {
    return new TwoController();
  }
}
