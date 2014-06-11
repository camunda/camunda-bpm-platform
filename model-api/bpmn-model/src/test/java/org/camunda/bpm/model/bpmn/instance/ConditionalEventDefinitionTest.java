package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.impl.instance.ConditionImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(Condition.class, 1, 1)
    );
  }

  @Test
  public void getEventDefinition() {
    ConditionalEventDefinition eventDefinition = eventDefinitionQuery.filterByType(ConditionalEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    Expression condition = eventDefinition.getCondition();
    assertThat(condition).isNotNull();
    assertThat(condition.getTextContent()).isEqualTo("${test}");
  }

}
