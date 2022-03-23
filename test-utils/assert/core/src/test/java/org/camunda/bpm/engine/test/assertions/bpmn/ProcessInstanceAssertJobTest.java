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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertJobTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_Single_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // Then
    assertThat(processInstance).job().isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_NullQuery_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    try {
      // When
      assertThat(processInstance).job((JobQuery) null);
    } catch (IllegalArgumentException e) {
      // Then
      assertThat(e).hasMessage("Illegal call of job(query = 'null') - but must not be null!");
    }
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_SingleWithQuery_Success() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // Then
    assertThat(processInstance).job(jobQuery().executable()).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_MultipleWithQuery_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // Then
    assertThat(processInstance).job("ServiceTask_1").isNotNull();
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(jobQuery().singleResult());
    // Then
    assertThat(processInstance).job("ServiceTask_2").isNotNull();
    // And
    assertThat(processInstance).job("ServiceTask_3").isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_NotYet_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).job("ServiceTask_2").isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_Passed_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // Then
    Mocks.register("serviceTask_1", "someService");
    // When
    execute(jobQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).task("ServiceTask_1").isNotNull();
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_MultipleWithQuery_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    execute(jobQuery().singleResult());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).job(jobQuery().executable()).isNotNull();
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_MultipleWithTaskDefinitionKey_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    execute(jobQuery().singleResult());
    // Then
    assertThat(processInstance).job("ServiceTask_2").isNotNull();
    // And
    assertThat(processInstance).job("ServiceTask_3").isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-job.bpmn"
  })
  public void testJob_MultipleWithTaskDefinitionKey_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    execute(jobQuery().list().get(0));
    // And
    Mocks.register("serviceTask_2", "someService");
    execute(jobQuery().list().get(0));
    // And
    Mocks.register("serviceTask_3", "someService");
    execute(jobQuery().list().get(0));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).job("ServiceTask_4").isNotNull();
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-jobEventSubprocess.bpmn"})
  public void testJob_MultipleAsyncAndEventSubprocessTimerStartWithActivityId_Success() {
    // When
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
        "ProcessInstanceAssert-jobEventSubprocess"
    );
    // Then
    assertThat(processInstance).job("ServiceTask_1").isNotNull();
    assertThat(processInstance).job("StartEvent_2").isNotNull();
  }
}
