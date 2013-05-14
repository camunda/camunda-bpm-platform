package org.camunda.bpm.cockpit.plugin.spi.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;

/**
 *
 * @author Nico Rehwaldt
 */
public abstract class AbstractCockpitPlugin implements CockpitPlugin {

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
