package org.camunda.bpm.cockpit.plugin;

import java.util.List;
import org.camunda.bpm.cockpit.plugin.api.Plugin;
import org.camunda.bpm.cockpit.plugin.api.PluginRegistry;
import org.camunda.bpm.cockpit.plugin.api.impl.PluginRegistryImpl;

/**
 *
 * @author nico.rehwaldt
 */
public class CockpitPlugins {

  private static final PluginRegistry pluginRegistry = new PluginRegistryImpl();

  public static PluginRegistry getRegistry() {
    return pluginRegistry;
  }

  public static List<Plugin> getPlugins() {
    return pluginRegistry.getRegisteredPlugins();
  }
}
