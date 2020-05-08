/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
