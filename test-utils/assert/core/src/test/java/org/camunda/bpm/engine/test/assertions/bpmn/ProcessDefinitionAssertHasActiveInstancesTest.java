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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processDefinitionQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessDefinitionAssertHasActiveInstancesTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_One_Started_Success() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // Then
    assertThat(processDefinition).hasActiveInstances(1);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_One_Started_Failure() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(0);
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(2);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_Two_Started_Success() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // Then
    assertThat(processDefinition).hasActiveInstances(2);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_Two_Started_Failure() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(0);
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(1);
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(3);
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_Two_Started_One_Ended_Success() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    complete(task(processInstance));
    // Then
    assertThat(processDefinition).hasActiveInstances(1);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessDefinitionAssert-hasActiveInstances.bpmn"
  })
  public void testHasActiveInstances_Two_Started_One_Ended_Failure() {
    // Given
    final ProcessDefinition processDefinition =
      processDefinitionQuery().processDefinitionKey("ProcessDefinitionAssert-hasActiveInstances").singleResult();
    // When
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessDefinitionAssert-hasActiveInstances"
    );
    // And
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(0);
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processDefinition).hasActiveInstances(2);
      }
    });
  }

}
