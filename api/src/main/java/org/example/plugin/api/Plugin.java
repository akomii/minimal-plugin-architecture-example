package org.example.plugin.api;

/**
 * Core interface for all external plugins. The host discovers implementations using the Java ServiceLoader mechanism.
 */
public interface Plugin {

  Object getController();
}
