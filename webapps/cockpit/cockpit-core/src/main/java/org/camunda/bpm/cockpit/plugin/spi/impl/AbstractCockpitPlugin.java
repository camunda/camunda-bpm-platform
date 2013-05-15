package org.camunda.bpm.cockpit.plugin.spi.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;

/**
 * An implementation of {@link CockpitPlugin} that comes with reasonable defaults.
 *
 * @author nico.rehwaldt
 *
 * @see CockpitPlugin
 */
public abstract class AbstractCockpitPlugin implements CockpitPlugin {

  /**
   * Returns a uri to a plugins asset directory.
   * The directory must be unique across all plugins.
   *
   * <p>
   *
   * This implementation assumes that the assets are provided in the directory <code>assets</code>,
   * relative to the location of the {@link CockpitPlugin} SPI implementation class.
   *
   * @return the directory providing the plugins client side assets
   */
  @Override
  public String getAssetDirectory() {
    return String.format("assets", getId());
  }

  @Override
  public Set<Class<?>> getResourceClasses() {
    return Collections.EMPTY_SET;
  }

  @Override
  public List<String> getMappingFiles() {
    return Collections.EMPTY_LIST;
  }
}
