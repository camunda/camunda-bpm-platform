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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.DEFAULT_WORKER_EXTERNAL_TASK;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTaskQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.fetchAndLock;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineTestsCompleteExternalTaskTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_TaskOnly_Success() {
    // Given
    ProcessInstance processInstance = getProcessInstanceStarted();
    assertThat(processInstance).hasNotPassed("ExternalTask_1");
    // When
    complete(externalTask(processInstance));
    // Then
    assertThat(processInstance).hasPassed("ExternalTask_1");
    assertThat(processInstance).isEnded();
    // And
    assertThat(getExternalTaskLogEntry().getWorkerId()).isEqualTo(DEFAULT_WORKER_EXTERNAL_TASK);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_TaskOnly_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    final ExternalTask task = externalTask(processInstance);
    // When
    complete(task);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(task);
      }
    }, NotFoundException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_NullTaskOnly_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete((ExternalTask) null);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_TaskOnlyMultiplePerTopic_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    getProcessInstanceStarted();
    // And
    final ExternalTask task = externalTask(processInstance);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        // one of the two complete-calls will hit the external task of the other process instance
        complete(task);
        complete(task);
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_WithVariables_Success() {
    // Given
    ProcessInstance processInstance = getProcessInstanceStarted();
    // When
    complete(externalTask(processInstance), withVariables("a", "b"));
    // Then
    assertThat(processInstance).isEnded().variables().containsEntry("a", "b");
    // And
    assertThat(getExternalTaskLogEntry().getWorkerId()).isEqualTo(DEFAULT_WORKER_EXTERNAL_TASK);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_WithVariables_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    final ExternalTask task = externalTask(processInstance);
    // When
    complete(task, withVariables("a", "b"));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(task, withVariables("a", "b"));
      }
    }, NotFoundException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_WithVariablesNullTask_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete((ExternalTask) null, withVariables("a", "b"));
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_WithNullVariables_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    final ExternalTask task = externalTask(processInstance);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(task, (Map<String, Object>) null);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_LockedTask_Success() {
    // Given
    ProcessInstance processInstance = getProcessInstanceStarted();
    assertThat(processInstance).hasNotPassed("ExternalTask_1");
    ExternalTask task = externalTask();
    // When
    List<LockedExternalTask> lockedTasks = fetchAndLock(task.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    assertThat(lockedTasks).hasSize(1);
    complete(lockedTasks.get(0));
    // Then
    assertThat(processInstance).hasPassed("ExternalTask_1");
    assertThat(processInstance).isEnded();
    // And
    assertThat(getExternalTaskLogEntry().getWorkerId()).isEqualTo(DEFAULT_WORKER_EXTERNAL_TASK);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_LockedTask_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    ExternalTask task = externalTask(processInstance);
    // When
    final List<LockedExternalTask> lockedTasks = fetchAndLock(task.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    assertThat(lockedTasks).hasSize(1);
    complete(lockedTasks.get(0));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(lockedTasks.get(0));
      }
    }, NotFoundException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_NullLockedTask_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete((LockedExternalTask) null);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_LockedTaskWithVariables_Success() {
    // Given
    ProcessInstance processInstance = getProcessInstanceStarted();
    assertThat(processInstance).hasNotPassed("ExternalTask_1");
    ExternalTask task = externalTask();
    // When
    List<LockedExternalTask> lockedTasks = fetchAndLock(task.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    assertThat(lockedTasks).hasSize(1);
    complete(lockedTasks.get(0), withVariables("a", "b"));
    // Then
    assertThat(processInstance).hasPassed("ExternalTask_1");
    assertThat(processInstance).isEnded().variables().containsEntry("a", "b");
    // And
    assertThat(getExternalTaskLogEntry().getWorkerId()).isEqualTo(DEFAULT_WORKER_EXTERNAL_TASK);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_LockedTaskWithVariables_Failure() {
    // Given
    final ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    ExternalTask task = externalTask(processInstance);
    // When
    final List<LockedExternalTask> lockedTasks = fetchAndLock(task.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    assertThat(lockedTasks).hasSize(1);
    complete(lockedTasks.get(0), withVariables("a", "b"));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(lockedTasks.get(0), withVariables("a", "b"));
      }
    }, NotFoundException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_NullLockedTaskWithVariables_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete((LockedExternalTask) null, withVariables("a", "b"));
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-completeExternalTask.bpmn" })
  public void testComplete_LockedTaskWithNullVariables_Failure() {
    // Given
    ProcessInstance processInstance = getProcessInstanceStarted();
    // And
    ExternalTask task = externalTask(processInstance);
    // When
    final List<LockedExternalTask> lockedTasks = fetchAndLock(task.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    assertThat(lockedTasks).hasSize(1);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        complete(lockedTasks.get(0), null);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ExternalTaskAssert-localVariables.bpmn" })
  public void testComplete_LockedTaskWithLocalVariables_Success() {
    // Given
    ProcessInstance pi = runtimeService().startProcessInstanceByKey("ExternalTaskAssert-localVariables");

    // Assume
    assertThat(externalTaskQuery().singleResult()).isNotNull();
    assertThat(externalTaskQuery().singleResult()).hasTopicName("External_1");

    LockedExternalTask task = fetchAndLock("External_1", "worker1", 1).get(0);
    assertThat(task.getActivityId()).isEqualTo("ExternalTask_1");

    // When
    complete(
      task,
      Collections.EMPTY_MAP,
      withVariables(
        "local_variable_1", "value_1"));

    // Then
    assertThat(externalTaskQuery().singleResult()).isNotNull();
    assertThat(externalTaskQuery().singleResult()).hasTopicName("Noop");
    assertThat(pi).variables().containsKey("variable_1");
  }

  @Test
  @Deployment(resources = { "bpmn/ExternalTaskAssert-localVariables.bpmn" })
  public void testComplete_LockedTaskWithLocalVariables_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey("ExternalTaskAssert-localVariables");

    // Assume
    assertThat(externalTaskQuery().singleResult()).isNotNull();

    // When
    LockedExternalTask task = fetchAndLock("External_1", "worker1", 1).get(0);
    assertThat(task.getActivityId()).isEqualTo("ExternalTask_1");

    // Then
    expect(()->complete(task), ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ExternalTaskAssert-localVariables.bpmn" })
  public void testCompleteTaskWithLocalVariables_Success() {
    // Given
    ProcessInstance pi = runtimeService().startProcessInstanceByKey("ExternalTaskAssert-localVariables");

    // Assume
    assertThat(externalTaskQuery().singleResult()).isNotNull();
    assertThat(externalTaskQuery().singleResult()).hasTopicName("External_1");

    // When
    complete(
      externalTaskQuery().singleResult(),
        Collections.EMPTY_MAP,
        withVariables(
          "local_variable_1", "value_1"));

    // Then
    assertThat(externalTaskQuery().singleResult()).isNotNull();
    assertThat(externalTaskQuery().singleResult()).hasTopicName("Noop");
    assertThat(pi).variables().containsKey("variable_1");
  }

  @Test
  @Deployment(resources = { "bpmn/ExternalTaskAssert-localVariables.bpmn" })
  public void testCompleteTaskWithoutLocalVariables_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey("ExternalTaskAssert-localVariables");

    // Assumen
    assertThat(externalTaskQuery().singleResult()).isNotNull();

    // When & Then
    expect(()->complete(externalTaskQuery().singleResult()), ProcessEngineException.class);
  }

  private ProcessInstance getProcessInstanceStarted() {
    return runtimeService().startProcessInstanceByKey("ProcessEngineTests-completeExternalTask");
  }

  private HistoricExternalTaskLog getExternalTaskLogEntry() {
    return historyService().createHistoricExternalTaskLogQuery().successLog().singleResult();
  }
}
