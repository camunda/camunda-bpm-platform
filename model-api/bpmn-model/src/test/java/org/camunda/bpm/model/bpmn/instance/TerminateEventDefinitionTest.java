package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TerminateEventDefinitionTest extends AbstractEventDefinitionTest {

  @Test
  public void getEventDefinition() {
    TerminateEventDefinition eventDefinition = eventDefinitionQuery.filterByType(TerminateEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
  }

}
