package org.example.plugin.two;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class TwoPlugin implements Plugin {

  @Override
  public Object getController() {
    return new TwoController();
  }
}
