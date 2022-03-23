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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineTestsJobTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(job()).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        job();
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    assertThat(processInstance).isNotNull();
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(job());
    // When
    expect(new Failure() {
      @Override
      public void when() {
        job();
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_activityId_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(job("ServiceTask_1")).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_activityId_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // When
    assertThat(processInstance).isNotNull();
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(job());
    // Then
    assertThat(job("ServiceTask_2")).isNotNull();
    // And
    assertThat(job("ServiceTask_3")).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_activityId_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        job("ServiceTask_1");
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobQuery_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // When
    assertThat(processInstance).isNotNull();
    // Then
    assertThat(job(jobQuery())).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobQuery_OnlyActivity_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        job(jobQuery());
      }
    }, IllegalStateException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobQuery_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    assertThat(processInstance).isNotNull();
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(job());
    // When
    expect(new Failure() {
      @Override
      public void when() {
        job(jobQuery());
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    assertThat(job(processInstance)).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_TwoActivities_processInstance_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(job(processInstance));
    // When
    expect(new Failure() {
      @Override
      public void when() {
        job(processInstance);
      }
    }, ProcessEngineException.class);
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobDefinitionKey_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    assertThat(job("ServiceTask_1", processInstance)).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobDefinitionKey_processInstance_TwoActivities_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    // When
    execute(job(processInstance));
    // Then
    assertThat(job("ServiceTask_2", processInstance)).isNotNull();
    // And
    assertThat(job("ServiceTask_3", processInstance)).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobQuery_processInstance_OnlyActivity_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // Then
    assertThat(job(jobQuery(), processInstance)).isNotNull();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessEngineTests-job.bpmn"
  })
  public void testJob_jobQuery_processInstance_TwoActivities_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    runtimeService().startProcessInstanceByKey(
      "ProcessEngineTests-job"
    );
    // And
    Mocks.register("serviceTask_1", "someService");
    // And
    execute(job(processInstance));
    // When
    expect(new Failure() {
      @Override
      public void when() {
        job(jobQuery(), processInstance);
      }
    }, ProcessEngineException.class);
  }

}
