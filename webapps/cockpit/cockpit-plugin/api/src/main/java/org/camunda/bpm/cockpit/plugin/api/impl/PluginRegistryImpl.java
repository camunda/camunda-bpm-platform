package org.camunda.bpm.cockpit.plugin.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.plugin.api.Plugin;
import org.camunda.bpm.cockpit.plugin.api.PluginRegistry;

/**
 *
 * @author nico.rehwaldt
 */
public class PluginRegistryImpl implements PluginRegistry {

  private Map<String, Plugin> plugins = new HashMap<String, Plugin>();

  @Override
  public void registerPlugin(Plugin plugin) {
    plugins.put(plugin.getId(), plugin);

    System.out.println(this.hashCode());
    plugins.put(plugin.getId(), plugin);

    System.out.println("[cockpit plugin registry] " + this);
    System.out.println("asdf");
    System.out.println("[cockpit plugin registry] registered plugin " + plugin.getId());
  }

  @Override
  public void unregisterPlugin(String id) {
    plugins.remove(id);
    System.out.println(this.hashCode());
    System.out.println("[cockpit plugin registry] unregistered plugin " + id);
  }

  @Override
  public List<Plugin> getRegisteredPlugins() {
    System.out.println(this.hashCode());
    return new ArrayList<Plugin>(plugins.values());
  }
}
