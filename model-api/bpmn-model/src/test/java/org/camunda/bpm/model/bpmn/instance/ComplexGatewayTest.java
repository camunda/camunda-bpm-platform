package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ComplexGatewayTest extends AbstractGatewayTest<ComplexGateway> {

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(ActivationCondition.class, 0, 1)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("default")
    );
  }

  @Test
  public void getDefault() {
    assertThat(gateway.getDefault().getId()).isEqualTo("flow");
  }

  @Test
  public void getActivationCondition() {
    assertThat(gateway.getActivationCondition().getTextContent()).isEqualTo("${test}");
  }

}
