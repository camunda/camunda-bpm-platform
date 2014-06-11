package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class InclusiveGatewayTest extends AbstractGatewayTest<InclusiveGateway> {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("default")
    );
  }

  @Test
  public void getDefault() {
    assertThat(gateway.getDefault().getId()).isEqualTo("flow");
  }

}
