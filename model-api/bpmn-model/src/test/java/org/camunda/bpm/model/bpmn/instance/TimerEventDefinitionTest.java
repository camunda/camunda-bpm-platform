package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TimerEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(TimeDate.class, 0, 1),
      new ChildElementAssumption(TimeDuration.class, 0, 1),
      new ChildElementAssumption(TimeCycle.class, 0, 1)
    );
  }

  @Test
  public void getElementDefinition() {
    List<TimerEventDefinition> eventDefinitions = eventDefinitionQuery.filterByType(TimerEventDefinition.class).list();
    assertThat(eventDefinitions).hasSize(3);
    for (TimerEventDefinition eventDefinition : eventDefinitions) {
      String id = eventDefinition.getId();
      String textContent = null;
      if (id.equals("date")) {
        textContent = eventDefinition.getTimeDate().getTextContent();
      }
      else if (id.equals("duration")) {
        textContent = eventDefinition.getTimeDuration().getTextContent();
      }
      else if (id.equals("cycle")) {
        textContent = eventDefinition.getTimeCycle().getTextContent();
      }

      assertThat(textContent).isEqualTo("${test}");
    }
  }

}
