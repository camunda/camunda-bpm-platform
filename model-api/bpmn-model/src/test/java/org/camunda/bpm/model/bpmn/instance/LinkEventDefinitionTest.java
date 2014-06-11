package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.impl.instance.Source;
import org.camunda.bpm.model.bpmn.impl.instance.Target;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkEventDefinitionTest extends AbstractEventDefinitionTest {

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(Source.class),
      new ChildElementAssumption(Target.class, 0, 1)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("name", false, true)
    );
  }

  @Test
  public void getEventDefinition() {
    LinkEventDefinition eventDefinition = eventDefinitionQuery.filterByType(LinkEventDefinition.class).singleResult();
    assertThat(eventDefinition).isNotNull();
    assertThat(eventDefinition.getName()).isEqualTo("link");
    assertThat(eventDefinition.getSources().iterator().next().getName()).isEqualTo("link");
    assertThat(eventDefinition.getTarget().getName()).isEqualTo("link");
  }

}
