package org.example.plugin.api;

public interface Plugin {

  /**
   * Unique id for the plugin
   */
  String id();

  /**
   * Execute plugin action, returns a message/result
   */
  String execute(String input);
}
