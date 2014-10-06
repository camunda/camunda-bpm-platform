package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

public class SignalEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("signalRef"),
      new AttributeAssumption(CAMUNDA_NS, "async", false, false, false)
    );
  }

  @Test
  public void getEventDefinition() {
    SignalEventDefinition eventDefinition = eventDefinitionQuery.filterByType(SignalEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.isCamundaAsync()).isFalse();

    eventDefinition.setCamundaAsync(true);
    assertThat(eventDefinition.isCamundaAsync()).isTrue();

    Signal signal = eventDefinition.getSignal();
    assertThat(signal).isNotNull();
    assertThat(signal.getId()).isEqualTo("signal");
    assertThat(signal.getName()).isEqualTo("signal");
    assertThat(signal.getStructure().getId()).isEqualTo("itemDef");
  }

}
