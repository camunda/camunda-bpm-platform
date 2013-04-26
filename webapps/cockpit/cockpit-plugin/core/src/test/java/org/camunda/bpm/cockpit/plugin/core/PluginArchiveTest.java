package org.camunda.bpm.cockpit.plugin.core;

import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.testPlugin;
import static org.fest.assertions.Assertions.assertThat;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class PluginArchiveTest {

  @Test
  public void shouldContainTestResources() {

    JavaArchive testPluginArchive = testPlugin();

    String contents = testPluginArchive.toString(true);

    assertThat(contents)
        .contains("/META-INF/services/org.camunda.bpm.cockpit.plugin.core.spi.CockpitPlugin")
        .contains("/org/camunda/bpm/cockpit/test/plugin/TestPlugin.class")
        .contains("/org/camunda/bpm/cockpit/test/plugin/assets/test.txt")
        .contains("/org/camunda/bpm/cockpit/test/plugin/queries/simple.xml");
  }
}
