package org.example.plugin.four;

import com.google.auto.service.AutoService;
import org.example.plugin.api.Plugin;

@AutoService(Plugin.class)
public class FourPlugin implements Plugin {

  @Override
  public Object getController() {
    return new FourController();
  }
}
