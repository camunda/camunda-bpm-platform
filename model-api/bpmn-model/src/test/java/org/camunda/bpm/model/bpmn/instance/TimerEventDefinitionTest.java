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
