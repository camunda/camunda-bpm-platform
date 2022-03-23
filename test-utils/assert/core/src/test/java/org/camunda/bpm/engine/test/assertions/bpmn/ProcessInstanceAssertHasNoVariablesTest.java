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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertHasNoVariablesTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasNoVariables.bpmn"
  })
  public void testHasNoVariables_None_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasNoVariables"
    );
    // Then
    assertThat(processInstance).hasNoVariables();
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).hasNoVariables();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasNoVariables.bpmn"
  })
  public void testHasNoVariables_One_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasNoVariables", withVariables("aVariable", "aValue")
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasNoVariables();
      }
    });
    // When
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasNoVariables();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasNoVariables.bpmn"
  })
  public void testHasNoVariables_Two_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasNoVariables", withVariables("firstVariable", "firstValue", "secondVariable", "secondValue")
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasNoVariables();
      }
    });
    // When
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasNoVariables();
      }
    });
  }

}
