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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertHasPassedInOrderLoopTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasPassedInOrder-loop.bpmn"
  })
  public void testHasPassedInOrder_SeveralActivities_HistoricInstance() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasPassedInOrder-loop",
      withVariables("exit", false)
    );
    // When
    complete(taskQuery().taskDefinitionKey("UserTask_1").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_2").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_4").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_3").singleResult());
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_4").singleResult(), withVariables("exit", true));
    // And
    complete(taskQuery().taskDefinitionKey("UserTask_5").singleResult());
    // Then
    assertThat(processInstance).hasPassedInOrder("UserTask_1", "UserTask_2", "UserTask_5");
    // And
    assertThat(processInstance).hasPassedInOrder("UserTask_1", "UserTask_3", "UserTask_4", "UserTask_3", "UserTask_4", "UserTask_5");
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasPassedInOrder("UserTask_1", "UserTask_3", "UserTask_4", "UserTask_3", "UserTask_4", "UserTask_3", "UserTask_4", "UserTask_5");
      }
    });
  }

}
