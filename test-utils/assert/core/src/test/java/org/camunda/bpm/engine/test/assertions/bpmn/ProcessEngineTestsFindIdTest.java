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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.findId;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineTestsFindIdTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testFindPlainTaskByName() {
    // Given
    // Process model deployed
    // When
    String id = findId("Plain task");
    // Then
    assertThat(id).isEqualTo("PlainTask_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testFindEndEventByName() {
    // Given
    // Process model deployed
    // When
    String end = findId("End");
    // Then
    assertThat(end).isEqualTo("End_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testFindAttachedEventByName() {
    // Given
    // Process model deployed
    // When
    String attachedBoundaryEvent = findId("2 days");
    // Then
    assertThat(attachedBoundaryEvent).isEqualTo("n2Days_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testFindGatewayByName() {
    // Given
    // process model deployed
    // When
    String gateway = findId("Continue?");
    // Then
    assertThat(gateway).isEqualTo("Continue_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testNameNotFound() {
    // Given
    // Process model deployed
    // When
    // findId("This should not be found");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        findId("This should not be found");
      }
    }, "doesn't exist");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testNameNull() {
    // Given
    // Process model deployed
    // When
    // findId(null);
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        findId(null);
      }
    }, IllegalArgumentException.class);
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findTest.bpmn")
  public void testFindAllElements() {
    // Given
    // Process model deployed
    // When
    String start = findId("Start");
    String plainTask = findId("Plain task");
    String userTask = findId("User task");
    String receiveTask = findId("Receive task");
    String attachedBoundaryEvent = findId("2 days");
    String gateway = findId("Continue?");
    String end = findId("End");
    String messageEnd = findId("Message End");
    //Then
    assertThat(start).isEqualTo("Start_TestID");
    assertThat(plainTask).isEqualTo("PlainTask_TestID");
    assertThat(userTask).isEqualTo("UserTask_TestID");
    assertThat(receiveTask).isEqualTo("ReceiveTask_TestID");
    assertThat(attachedBoundaryEvent).isEqualTo("n2Days_TestID");
    assertThat(gateway).isEqualTo("Continue_TestID");
    assertThat(end).isEqualTo("End_TestID");
    assertThat(messageEnd).isEqualTo("MessageEnd_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findInTwoPools.bpmn")
  public void testFindInTwoPoolsInPool1() {
    // Given
    // Process model with two pools deployed
    // When
    String callActivity = findId("Call activity one");
    // Then
    assertThat(callActivity).isEqualTo("CallActivityOne_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findInTwoPools.bpmn")
  public void testFindTwoPoolsInPool2() {
    // Given
    // Process model with two pools deployed
    // When
    String task = findId("Subprocess task");
    // Then
    assertThat(task).isEqualTo("SubProcessTask_TestID");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-findTest.bpmn", "bpmn/ProcessEngineTests-findInTwoPools.bpmn"})
  public void testFindOneInEachOfTwoDiagrams() {
    // Given
    // Two process models deployed
    // When
    String start = findId("Start");
    String plainTask = findId("Plain task");
    String startSuperProcess = findId("Super started");
    String taskTwo = findId("Task two");
    String proc2Started = findId("Proc 2 started");
    // Then
    assertThat(start).isEqualTo("Start_TestID");
    assertThat(plainTask).isEqualTo("PlainTask_TestID");
    assertThat(startSuperProcess).isEqualTo("SuperStarted_TestID");
    assertThat(taskTwo).isEqualTo("TaskTwo_TestID");
    assertThat(proc2Started).isEqualTo("Proc2Started_TestID");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findDuplicateNames.bpmn")
  public void testProcessWithDuplicateNames() {
    // Given
    // Process model with duplicate task names deployed
    // When
    // findId("Task one"); findId("Event one"); findId("Gateway one");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        findId("Task one");
      }
    }, "not unique");
    // And
    expect(new Failure() {
      @Override
      public void when() {
        findId("Event one");
      }
    }, "not unique");
    // And
    expect(new Failure() {
      @Override
      public void when() {
        findId("Gateway one");
      }
    }, "not unique");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findDuplicateNamesOnTaskAndGateway.bpmn")
  public void testProcesswithDuplicateNamesOnDifferentElementsTypes() {
    // Given
    // Process model with same name on task and gateway deployed
    // When
    // findId("Element one");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        findId("Element one");
      }
    }, "not unique");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessEngineTests-findDuplicateNamesOnTaskAndGateway.bpmn")
  public void testProcessWithDuplicateNamesDindTheUniqueOnly() {
    // Given
    // Process model with two pools and a mix of duplicate and unique names deployed
    // When
    String startOne = findId("Start one");
    String endTwo = findId("End two");
    // Then
    assertThat(startOne).isEqualTo("StartOne_TestID");
    assertThat(endTwo).isEqualTo("EndTwo_TestID");
  }
}
