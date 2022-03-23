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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTaskQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineTestsExternalTaskTest extends ProcessAssertTestCase {

  private static final String EXTERNAL_TASK_3 = "ExternalTask_3";
  private static final String EXTERNAL_TASK_2 = "ExternalTask_2";
  private static final String EXTERNAL_TASK_1 = "ExternalTask_1";
  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(externalTask()).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        externalTask();
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_TwoActivities_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    assertThat(processInstance).isNotNull();
    // And
    assertThat(externalTask()).isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    assertThat(processInstance).isNotNull();
    // And
    complete(externalTask());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        externalTask();
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_ActivityId_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(externalTask(EXTERNAL_TASK_1)).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_ActivityId_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        task(EXTERNAL_TASK_1);
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_ActivityId_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    assertThat(processInstance).isNotNull();
    complete(externalTask());
    // Then
    assertThat(externalTask(EXTERNAL_TASK_2)).isNotNull().hasActivityId(EXTERNAL_TASK_2);
    // And
    assertThat(externalTask(EXTERNAL_TASK_3)).isNotNull().hasActivityId(EXTERNAL_TASK_3);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(externalTask(externalTaskQuery())).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        externalTask(externalTaskQuery());
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    assertThat(processInstance).isNotNull();
    complete(externalTask());
    // Then
    assertThat(externalTask(externalTaskQuery().activityId(EXTERNAL_TASK_2))).isNotNull().hasActivityId(EXTERNAL_TASK_2);
    // And
    assertThat(externalTask(externalTaskQuery().activityId(EXTERNAL_TASK_3))).isNotNull().hasActivityId(EXTERNAL_TASK_3);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    assertThat(processInstance).isNotNull();
    // And
    complete(externalTask());
    // When
    expect(new Failure() {
      @Override
      public void when() {
        externalTask(externalTaskQuery());
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    assertThat(externalTask(processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_processInstance_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    complete(externalTask(processInstance));
    // When
    expect(new Failure() {
      @Override
      public void when() {
        externalTask(processInstance);
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_ActivityId_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    assertThat(externalTask(EXTERNAL_TASK_1, processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_ActivityId_processInstance_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    complete(externalTask(processInstance));
    // Then
    assertThat(externalTask(EXTERNAL_TASK_2, processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_2);
    // And
    assertThat(externalTask(EXTERNAL_TASK_3, processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_3);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // Then
    assertThat(externalTask(externalTaskQuery().activityId(EXTERNAL_TASK_1), processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_1);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_processInstance_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // When
    complete(externalTask(processInstance));
    // Then
    assertThat(externalTask(externalTaskQuery().activityId(EXTERNAL_TASK_2), processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_2);
    // And
    assertThat(externalTask(externalTaskQuery().activityId(EXTERNAL_TASK_3), processInstance)).isNotNull().hasActivityId(EXTERNAL_TASK_3);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-externalTask.bpmn" })
  public void testTask_taskQuery_processInstance_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    runtimeService().startProcessInstanceByKey("ProcessEngineTests-externalTask");
    // And
    complete(externalTask(processInstance));
    // When
    expect(new Failure() {
      @Override
      public void when() {
        externalTask(externalTaskQuery(), processInstance);
      }
    }, ProcessEngineException.class);
  }

}
