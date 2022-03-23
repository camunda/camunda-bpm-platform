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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTaskQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertExternalTaskTest extends ProcessAssertTestCase {

  private static final String TASK1 = "ExternalTask_1";
  private static final String TASK2 = "ExternalTask_2";
  private static final String TASK3 = "ExternalTask_3";
  private static final String TASK4 = "ExternalTask_4";

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNoArgsForSingleTask_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    assertThat(processInstance).externalTask().isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNoArgsForMultipleTasks_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    complete(externalTask());
    try {
      // When
      assertThat(processInstance).externalTask();
    } catch (ProcessEngineException pe) {
      // Then
      assertThat(pe)
        .hasStackTraceContaining("org.camunda.bpm.engine.impl.AbstractQuery.executeSingleResult");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNullQuery_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    try {
      // When
      assertThat(processInstance).externalTask((ExternalTaskQuery) null);
    } catch (IllegalArgumentException iae) {
      // Then
      assertThat(iae)
        .hasMessage("Illegal call of externalTask(query = 'null') - but must not be null!");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testEmptyQueryForSingleTask_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    assertThat(processInstance).externalTask(externalTaskQuery()).isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testEmptyQueryForMultipleTasks_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    complete(externalTask());
    try {
      // When
      assertThat(processInstance).externalTask(externalTaskQuery());
    } catch (ProcessEngineException pe) {
      // Then
      assertThat(pe)
        .hasStackTraceContaining("org.camunda.bpm.engine.impl.AbstractQuery.executeSingleResult");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNarrowedQuery_Success() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    // When
    complete(externalTask());
    // Then
    assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK2)).isNotNull();
    // And
    assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK3)).isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNarrowedQuery_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    complete(externalTask());
    try {
      // When
      assertThat(processInstance).externalTask(externalTaskQuery().processInstanceId(processInstance.getId()));
    } catch (ProcessEngineException pe) {
      // Then
      assertThat(pe)
      .hasStackTraceContaining("org.camunda.bpm.engine.impl.AbstractQuery.executeSingleResult");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNotYetQuery_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK2)).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testAlreadyPassedQuery_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK1)).isNotNull();
    complete(externalTask());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK1)).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNullId_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    try {
      // When
      assertThat(processInstance).externalTask((String) null);
    } catch (IllegalArgumentException iae) {
      // Then
      assertThat(iae)
        .hasMessage("Illegal call of externalTask(activityId = 'null') - must not be null!");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testIdForSingleTask_Success() {
    // When
    final ProcessInstance processInstance = startProcess();
    // Then
    assertThat(processInstance).externalTask(TASK1).isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testIdForSingleTask_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    try {
      // When
      assertThat(processInstance).externalTask(TASK2).isNotNull();
    } catch (AssertionError e) {
      // Then
      assertThat(e).hasMessage("Expecting " + assertThat(processInstance).toString(processInstance)
          + " to be waiting at [ExternalTask_2], but it is actually waiting at [ExternalTask_1].");
    }
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testIdForMultipleTasks_Success() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    // When
    complete(externalTask());
    // Then
    assertThat(processInstance).externalTask(TASK2).isNotNull();
    // And
    assertThat(processInstance).externalTask(TASK3).isNotNull();
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testIdForMultipleTasks_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    complete(externalTask());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).externalTask(TASK4).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testNotYetId_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask().isNotNull();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).externalTask(TASK2).isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessInstanceAssert-externalTask.bpmn" })
  public void testAlreadyPassedId_Failure() {
    // Given
    final ProcessInstance processInstance = startProcess();
    assertThat(processInstance).externalTask(externalTaskQuery().activityId(TASK1)).isNotNull();
    complete(externalTask());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).externalTask(TASK1).isNotNull();
      }
    });
  }

  private ProcessInstance startProcess() {
    return runtimeService().startProcessInstanceByKey("ProcessInstanceAssert-externalTask");
  }

}
