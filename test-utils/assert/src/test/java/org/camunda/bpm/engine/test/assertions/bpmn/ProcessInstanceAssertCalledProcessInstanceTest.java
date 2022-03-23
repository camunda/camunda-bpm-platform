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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.calledProcessInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processInstanceQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertCalledProcessInstanceTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_FirstOfTwoSequential_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-calledProcessInstance-superProcess1"
    );
    // Then
    assertThat(processInstance)
      .calledProcessInstance()
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // And
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess1")
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // And
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess2")
      .isNull();
    // And
    assertThat(processInstance)
      .calledProcessInstance(processInstanceQuery())
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_SecondOfTwoSequential_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-calledProcessInstance-superProcess1"
    );
    // Then
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess2")
      .isNull();
    // And
    assertThat(processInstance)
      .calledProcessInstance()
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // When
    complete(task("UserTask_1", calledProcessInstance()));
    // Then
    assertThat(processInstance)
      .calledProcessInstance()
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess2");
    // And
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess1")
      .isNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_SecondOfTwoSequential_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-calledProcessInstance-superProcess1"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance)
          .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess2")
          .isNotNull();
      }
    });
    // And
    assertThat(processInstance)
      .calledProcessInstance()
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // When
    complete(task("UserTask_1", calledProcessInstance()));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess1")
          .isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess2.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_TwoOfTwoParallel_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-calledProcessInstance-superProcess2"
    );
    // Then
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess1")
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // And
    assertThat(processInstance)
      .calledProcessInstance(processInstanceQuery().processDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1"))
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess1");
    // And
    assertThat(processInstance)
      .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess2")
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess2");
    // And
    assertThat(processInstance)
      .calledProcessInstance(processInstanceQuery().processDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess2"))
      .hasProcessDefinitionKey("ProcessInstanceAssert-calledProcessInstance-subProcess2");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess2.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_TwoOfTwoParallel_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-calledProcessInstance-superProcess2"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance)
          .calledProcessInstance("ProcessInstanceAssert-calledProcessInstance-subProcess3")
          .isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-calledProcessInstance-superProcess2.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess1.bpmn", "bpmn/ProcessInstanceAssert-calledProcessInstance-subProcess2.bpmn"
  })
  public void testCalledProcessInstance_NullQuery_Failure() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
        "ProcessInstanceAssert-calledProcessInstance-superProcess2"
    );
    try {
      // When
      assertThat(processInstance).calledProcessInstance((ProcessInstanceQuery) null);
    } catch (IllegalArgumentException e) {
      // Then
      assertThat(e).hasMessage("Illegal call of calledProcessInstance(query = 'null') - but must not be null!");
    }
  }
}
