package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class CompensateEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("waitForCompletion"),
      new AttributeAssumption("activityRef")
    );
  }

  @Test
  public void getEventDefinition() {
    CompensateEventDefinition eventDefinition = eventDefinitionQuery.filterByType(CompensateEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.isWaitForCompletion()).isTrue();
    assertThat(eventDefinition.getActivity().getId()).isEqualTo("task");
  }

}
