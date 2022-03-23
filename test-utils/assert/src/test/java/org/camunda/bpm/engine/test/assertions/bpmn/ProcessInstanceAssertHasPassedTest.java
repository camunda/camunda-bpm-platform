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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertHasPassedTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_OnlyActivity_RunningInstance_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    assertThat(processInstance).hasPassed("UserTask_1");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_OnlyActivity_RunningInstance_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("UserTask_1");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_ParallelActivities_RunningInstance_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // Then
    assertThat(processInstance).hasPassed("UserTask_1");
    // And
    assertThat(processInstance).hasPassed("UserTask_2");
    // And
    assertThat(processInstance).hasPassed("UserTask_1", "UserTask_2");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_ParallelActivities_RunningInstance_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("UserTask_3");
      }
    });
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("UserTask_4");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_SeveralActivities_RunningInstance_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // Then
    assertThat(processInstance).hasPassed("UserTask_1");
    // And
    assertThat(processInstance).hasPassed("UserTask_2");
    // And
    assertThat(processInstance).hasPassed("UserTask_3");
    // And
    assertThat(processInstance).hasPassed("UserTask_1", "UserTask_2", "UserTask_3");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_SeveralActivities_RunningInstance_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("UserTask_4");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_SeveralActivities_HistoricInstance_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_4").singleResult());
    // Then
    assertThat(processInstance).hasPassed("UserTask_1");
    // And
    assertThat(processInstance).hasPassed("UserTask_2");
    // And
    assertThat(processInstance).hasPassed("UserTask_3");
    // And
    assertThat(processInstance).hasPassed("UserTask_4");
    // And
    assertThat(processInstance).hasPassed("UserTask_1", "UserTask_2", "UserTask_3", "UserTask_4");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassed.bpmn"
  })
  public void testHasPassed_SeveralActivities_HistoricInstance_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassed"
    );
    // When
    complete(taskQuery().singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_4").singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("UserTask_5");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isWaitingAt.bpmn"
  })
  public void testHasPassed_Null_Error() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingAt"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        String[] passed = null;
        assertThat(processInstance).hasPassed(passed);
      }
    });
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed("ok", null);
      }
    });
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassed(null, "ok");
      }
    });
    expect(new Failure() {
      @Override
      public void when() {
        String[] args = new String[]{};
        assertThat(processInstance).hasPassed(args);
      }
    });
  }

}
