package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class SignalEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("signalRef")
    );
  }

  @Test
  public void getEventDefinition() {
    SignalEventDefinition eventDefinition = eventDefinitionQuery.filterByType(SignalEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    Signal signal = eventDefinition.getSignal();
    assertThat(signal).isNotNull();
    assertThat(signal.getId()).isEqualTo("signal");
    assertThat(signal.getName()).isEqualTo("signal");
    assertThat(signal.getStructure().getId()).isEqualTo("itemDef");
  }

}
