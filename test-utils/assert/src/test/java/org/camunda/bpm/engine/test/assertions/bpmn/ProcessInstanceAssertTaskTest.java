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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertTaskTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_Single_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    assertThat(processInstance).task().isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_NullQuery_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    try {
      // When
      assertThat(processInstance).task((TaskQuery) null);
    } catch (IllegalArgumentException e) {
      // Then
      assertThat(e).hasMessage("Illegal call of task(query = 'null') - but must not be null!");
    }
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_SingleWithQuery_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_1")).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_MultipleWithQuery_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // And
    complete(taskQuery().singleResult());
    // Then
    assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_2")).isNotNull();
    // And
    assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_3")).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_NotYet_Failure() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_2")).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_Passed_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    // When
    complete(taskQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_1")).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_MultipleWithQuery_Failure() {
    // When
    final ProcessInstance processInstance = startProcess();
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_1").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task(taskQuery().taskDefinitionKey("UserTask_4")).isNotNull();
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_MultipleWithTaskDefinitionKey_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // And
    complete(taskQuery().singleResult());
    // Then
    assertThat(processInstance).task("UserTask_2").isNotNull();
    // And
    assertThat(processInstance).task("UserTask_3").isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_MultipleWithTaskDefinitionKey_Failure() {
    // When
    final ProcessInstance processInstance = startProcess();
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_1").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task("UserTask_4").isNotNull();
      }
    }, ProcessEngineException.class);
  }


  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-task.bpmn"
  })
  public void testTask_notWaitingAtTaskDefinitionKey() {
    final ProcessInstance processInstance = startProcess();
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task("UserTask_2").isNotNull();
      }
    });
  }

  private ProcessInstance startProcess() {
    return runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-task"
    );
  }

}
