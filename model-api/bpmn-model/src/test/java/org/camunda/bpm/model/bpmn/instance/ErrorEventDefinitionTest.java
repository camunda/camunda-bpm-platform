package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("errorRef")
    );
  }

  @Test
  public void getEventDefinition() {
    ErrorEventDefinition eventDefinition = eventDefinitionQuery.filterByType(ErrorEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.getError().getId()).isEqualTo("error");
  }

}
