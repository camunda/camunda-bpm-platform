package org.camunda.bpm.cockpit.plugin.core;

import org.camunda.bpm.cockpit.plugin.core.test.AbstractCockpitPluginTest;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.plugin.core.db.QueryParameters;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class PluginQueryTest extends AbstractCockpitPluginTest {


  @Test
  public void testCustomQuery() {

    List<Execution> result = getQueryService().executeQuery("cockpit.test.selectExecution", new QueryParameters<Execution>());

    assertThat(result).hasSize(0);
  }

  @Test
  public void testCustomQuery2() {

    List<Execution> result = getQueryService().executeQuery("cockpit.test.selectExecution", new QueryParameters<Execution>());

    assertThat(result).hasSize(0);
  }
}
