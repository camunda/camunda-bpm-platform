package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.EventBasedGatewayType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EventBasedGatewayTest extends AbstractGatewayTest<EventBasedGateway> {

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("instantiate", false, false, false),
      new AttributeAssumption("eventGatewayType", false, false, EventBasedGatewayType.Exclusive)
    );
  }

  @Test
  public void getInstantiate() {
    assertThat(gateway.isInstantiate()).isTrue();
  }

  @Test
  public void getEventGatewayType() {
    assertThat(gateway.getEventGatewayType()).isEqualTo(EventBasedGatewayType.Parallel);
  }

  @Test
  public void shouldFailSetAsyncAfterToEventBasedGateway() {
    // fetching should fail
    try {
      gateway.isCamundaAsyncAfter();
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }

    // set the attribute should fail to!
    try {
      gateway.setCamundaAsyncAfter(false);
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }
  }
}
