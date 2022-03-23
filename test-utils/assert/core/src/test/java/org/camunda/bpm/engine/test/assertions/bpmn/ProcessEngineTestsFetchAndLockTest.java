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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.fetchAndLock;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineTestsFetchAndLockTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-fetchAndLock.bpmn" })
  public void testFetchAndLock_Success() {
    // Given
    getProcessInstanceStarted();
    // When
    List<LockedExternalTask> lockedTasks = fetchAndLock("External_1", DEFAULT_WORKER_EXTERNAL_TASK, 1);
    // Then
    assertThat(lockedTasks).hasSize(1);
    // And
    assertThat(lockedTasks.get(0).getWorkerId()).isEqualTo(DEFAULT_WORKER_EXTERNAL_TASK);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-fetchAndLock.bpmn" })
  public void testFetchAndLock_Failure() {
    // Given
    getProcessInstanceStarted();
    // When
    fetchAndLock("External_1", DEFAULT_WORKER_EXTERNAL_TASK, 1);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(fetchAndLock("External_1", DEFAULT_WORKER_EXTERNAL_TASK, 1)).isNotEmpty();
      }
    });
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-fetchAndLock.bpmn" })
  public void testFetchAndLock_NullTopic_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        fetchAndLock(null, DEFAULT_WORKER_EXTERNAL_TASK, 1);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = { "bpmn/ProcessEngineTests-fetchAndLock.bpmn" })
  public void testFetchAndLock_NullWorker_Failure() {
    // Given
    getProcessInstanceStarted();
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        fetchAndLock("External_1", null, 1);
      }
    }, IllegalArgumentException.class);
  }

  private ProcessInstance getProcessInstanceStarted() {
    return runtimeService().startProcessInstanceByKey("ProcessEngineTests-fetchAndLock");
  }
}
