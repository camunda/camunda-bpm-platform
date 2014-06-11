package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class EscalationEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("escalationRef")
    );
  }

  @Test
  public void getEventDefinition() {
    EscalationEventDefinition eventDefinition = eventDefinitionQuery.filterByType(EscalationEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.getEscalation().getName()).isEqualTo("escalation");
    assertThat(eventDefinition.getEscalation().getEscalationCode()).isEqualTo("1337");
    assertThat(eventDefinition.getEscalation().getStructure().getId()).isEqualTo("itemDef");
  }

}
