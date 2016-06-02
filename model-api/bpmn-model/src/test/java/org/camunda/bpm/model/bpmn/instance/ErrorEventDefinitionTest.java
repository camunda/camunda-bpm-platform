package org.camunda.bpm.model.bpmn.instance;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("errorRef"),
      new AttributeAssumption(CAMUNDA_NS, "errorCodeVariable"),
      new AttributeAssumption(CAMUNDA_NS, "errorMessageVariable")
    );
  }

  @Test
  public void getEventDefinition() {
    ErrorEventDefinition eventDefinition = eventDefinitionQuery.filterByType(ErrorEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.getError().getId()).isEqualTo("error");
  }
  
}
