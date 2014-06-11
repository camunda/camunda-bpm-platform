package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CancelEventDefinitionTest extends AbstractEventDefinitionTest {

  @Test
  public void getEventDefinition() {
    CancelEventDefinition eventDefinition = eventDefinitionQuery.filterByType(CancelEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
  }

}
