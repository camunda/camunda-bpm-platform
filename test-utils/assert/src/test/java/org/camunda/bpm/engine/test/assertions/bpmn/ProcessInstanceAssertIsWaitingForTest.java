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
package org.camunda.bpm.engine.test.assertions.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertIsWaitingForTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor.bpmn"
  })
  public void testIsWaitingFor_One_Message_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor"
    );
    // Then
    assertThat(processInstance).isWaitingFor("myMessage");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor-2.bpmn"
  })
  public void testIsWaitingFor_Two_Messages_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor-2"
    );
    // Then
    assertThat(processInstance).isWaitingFor("myMessage", "yourMessage");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor-2.bpmn"
  })
  public void testIsWaitingFor_One_Of_Two_Messages_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor-2"
    );
    // When
    runtimeService().correlateMessage("myMessage");
    // Then
    assertThat(processInstance).isWaitingFor("yourMessage");
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor("myMessage");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor.bpmn"
  })
  public void testIsWaitingFor_One_Message_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor"
    );
    // When
    runtimeService().correlateMessage("myMessage");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor("myMessage");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor.bpmn"
  })
  public void testIsWaitingFor_Not_Waiting_For_One_Of_One_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor("yourMessage");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor.bpmn"
  })
  public void testIsWaitingFor_Not_Waiting_For_One_Of_Two_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor("myMessage", "yourMessage");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingFor.bpmn"
  })
  public void testIsWaitingFor_Null_Error() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingFor"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor();
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        String[] waitingFor = null;
        assertThat(processInstance).isWaitingFor(waitingFor);
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingFor("myMessage", null);
      }
    });
  }

}
