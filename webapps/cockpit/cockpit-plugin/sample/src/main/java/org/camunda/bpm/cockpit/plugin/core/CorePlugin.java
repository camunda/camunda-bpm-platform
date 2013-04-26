package org.camunda.bpm.cockpit.plugin.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.core.spi.impl.AbstractCockpitPlugin;

/**
 *
 * @author nico.rehwaldt
 */
public class CorePlugin extends AbstractCockpitPlugin {

  private static final String[] MAPPING_FILES = {
//    "org/camunda/bpm/cockpit/plugin/sample/queries/auditMapping.xml",
//    "org/camunda/bpm/cockpit/plugin/sample/queries/processInstanceMapping.xml",
//    "org/camunda/bpm/cockpit/plugin/sample/queries/variableMapping.xml",

    "org/camunda/bpm/cockpit/plugin/sample/queries/simple.xml"
  };

  @Override
  public String getId() {
    return "sample";
  }

  @Override
  public List<String> getMappingFiles() {
    return Arrays.asList(MAPPING_FILES);
  }
}
