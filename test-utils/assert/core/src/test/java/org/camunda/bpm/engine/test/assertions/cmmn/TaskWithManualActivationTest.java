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
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseExecution;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseService;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.disable;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.manuallyStart;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class TaskWithManualActivationTest extends ProcessAssertTestCase {

  public static final String TASK_A = "PI_TaskA";
  public static final String CASE_KEY = "Case_TaskTests";

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = { "cmmn/TaskWithManualActivationTest.cmmn" })
  public void task_should_be_enabled() {
    // Given
    CaseInstance caseInstance = caseService().createCaseInstanceByKey(CASE_KEY);
    // Then
    assertThat(caseExecution(TASK_A, caseInstance)).isEnabled();
  }

  @Test
  @Deployment(resources = { "cmmn/TaskWithManualActivationTest.cmmn" })
  public void task_should_be_active_when_manually_started() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreated();
    // When
    manuallyStart(caseExecution(TASK_A, caseInstance));
    // Then
    assertThat(caseExecution(TASK_A, caseInstance)).isActive();
  }

  /**
   * Introduces:
   * disable(caseExecution)
   * task.isDisabled()
   */
  @Test
  @Deployment(resources = { "cmmn/TaskWithManualActivationTest.cmmn" })
  public void task_should_be_disabed() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreated();
    // When
    CaseExecution taskA;
    disable(taskA = caseExecution(TASK_A, caseInstance));
    // Then
    assertThat(caseInstance).isCompleted();
    assertThat(taskA).isDisabled();
  }

  private CaseInstance givenCaseIsCreated() {
    return caseService().createCaseInstanceByKey(CASE_KEY);
  }
}
