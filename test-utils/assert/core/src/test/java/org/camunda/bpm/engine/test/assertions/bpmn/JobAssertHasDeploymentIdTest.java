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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processDefinitionQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processInstanceQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class JobAssertHasDeploymentIdTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/JobAssert-hasDeploymentId.bpmn"
  })
  public void testHasDeploymentId_Success() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "JobAssert-hasDeploymentId"
    );
    // Then
    assertThat(jobQuery().singleResult()).isNotNull();
    // And
    assertThat(jobQuery().singleResult()).hasDeploymentId(processDefinitionQuery().processDefinitionId(processInstanceQuery().singleResult().getProcessDefinitionId()).singleResult().getDeploymentId());
  }

  @Test
  @Deployment(resources = {"bpmn/JobAssert-hasDeploymentId.bpmn"
  })
  public void testHasDeploymentId_Failure() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "JobAssert-hasDeploymentId"
    );
    // Then
    assertThat(jobQuery().singleResult()).isNotNull();
    // And
    expect(new Failure() {
      @Override
      public void when() {
        BpmnAwareTests.assertThat(jobQuery().singleResult()).hasDeploymentId("otherDeploymentId");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/JobAssert-hasDeploymentId.bpmn"
  })
  public void testHasDeploymentId_Error_Null() {
    // Given
    runtimeService().startProcessInstanceByKey(
      "JobAssert-hasDeploymentId"
    );
    // Then
    assertThat(jobQuery().singleResult()).isNotNull();
    // And
    expect(new Failure() {
      @Override
      public void when() {
        BpmnAwareTests.assertThat(jobQuery().singleResult()).hasDeploymentId(null);
      }
    });
  }

}
