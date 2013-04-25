package org.camunda.bpm.cockpit.plugin.api;

import java.util.List;

/**
 *
 * @author nico.rehwaldt
 */
public interface PluginRegistry {

  /**
   * Registers a plugin
   *
   * @param plugin
   */
  public void registerPlugin(Plugin plugin);

  /**
   * Unregisters a plugin
   */
  public void unregisterPlugin(String id);

  /**
   * Return a list of registered plugins
   * 
   * @return
   */
  public List<Plugin> getRegisteredPlugins();
}
