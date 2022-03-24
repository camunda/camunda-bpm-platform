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
package org.camunda.bpm.engine.test.assertions.cmmn;

import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseService;

import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class HumanTaskAssertVariablesTest extends ProcessAssertTestCase {

  public static final String TASK_A = "PI_TaskA";
  public static final String CASE_KEY = "Case_HumanTaskAssert-variables";

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testVariables_Success() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When
    setVariablesOnHumanTask();

    // Then
    assertThat(caseInstance).humanTask(TASK_A).variables().containsEntry("aVariable", "aValue");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().containsEntry("bVariable", "bValue");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().containsKey("aVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().containsKey("bVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().containsKeys("aVariable", "bVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().containsValue("aValue");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().doesNotContainValue("cValue");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().doesNotContainEntry("cVariable", "cValue");
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().isNotEmpty();
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testVariables_Success_No_Variables() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When

    // Then
    assertThat(caseInstance).humanTask(TASK_A).variables().isEmpty();
    // And
    assertThat(caseInstance).humanTask(TASK_A).variables().doesNotContainEntry("aVariable", "aValue");
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testVariables_Failure_HumanTask_Completed() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When
    setAVariableOnHumanTaskAndCompleteHumanTask();

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(caseInstance).humanTask(TASK_A).variables().containsEntry("aVariable", "aValue");
      }
    });
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasVariables_Success() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When
    setVariablesOnHumanTask();

    // Then
    assertThat(caseInstance).humanTask(TASK_A).hasVariables("aVariable", "bVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).hasVariables("aVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).hasVariables("bVariable");
    // And
    assertThat(caseInstance).humanTask(TASK_A).hasVariables();
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasVariables_Failure() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(caseInstance).humanTask(TASK_A).hasVariables();
      }
    });
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasNoVariables_Success() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When

    // Then
    assertThat(caseInstance).humanTask(TASK_A).hasNoVariables();
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasNoVariables_Failure() {
    // Given
    final CaseInstance caseInstance = createCaseInstance();

    // When
    setVariablesOnHumanTask();

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(caseInstance).humanTask(TASK_A).hasNoVariables();
      }
    });
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testVariables_Success_NoHumanTaskVariables() {
    // Given
    final CaseInstance caseInstance = createCaseInstanceWithVariable();

    // When

    // Then
    assertThat(caseInstance).humanTask(TASK_A).variables().isEmpty();
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testVariables_Failure_NoHumanTaskVariables() {
    // Given
    final CaseInstance caseInstance = createCaseInstanceWithVariable();

    // When

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(caseInstance).humanTask(TASK_A).variables().isNotEmpty();
      }
    });
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasNoVariables_Success_NoHumanTaskVariables() {
    // Given
    final CaseInstance caseInstance = createCaseInstanceWithVariable();

    // When

    // Then
    assertThat(caseInstance).humanTask(TASK_A).hasNoVariables();
  }

  @Test
  @Deployment(resources = { "cmmn/HumanTaskAssert-variables.cmmn" })
  public void testHasVariables_Failure_NoHumanTaskVariables() {
    // Given
    final CaseInstance caseInstance = createCaseInstanceWithVariable();

    // When

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(caseInstance).humanTask(TASK_A).hasVariables();
      }
    });
  }

  private CaseInstance createCaseInstance() {
    return caseService().createCaseInstanceByKey(CASE_KEY);
  }

  private CaseInstance createCaseInstanceWithVariable() {
    CaseInstance caseInstance = createCaseInstance();
    caseExecution(caseInstance.getActivityId()).setVariable("aVariable", "aValue");
    return caseInstance;
  }

  private void setAVariableOnHumanTaskAndCompleteHumanTask() {
    caseExecution(TASK_A).setVariable("aVariable", "aValue").complete();
  }

  private void setVariablesOnHumanTask() {
    caseExecution(TASK_A).setVariable("aVariable", "aValue").setVariable("bVariable", "bValue").execute();
  }

  private CaseExecutionCommandBuilder caseExecution(String activityId) {
    return caseService().withCaseExecution(caseService().createCaseExecutionQuery().activityId(activityId).singleResult().getId());
  }

}