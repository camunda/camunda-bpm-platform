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
package org.camunda.bpm.engine.test.bpmn.behavior;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BpmnBehaviorLoggerTest extends PluggableProcessEngineTest {

  @After
  public void tearDown() {
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(false);
  }

  @Rule
  public ProcessEngineLoggingRule processEngineLoggingRule = new ProcessEngineLoggingRule().watch(
      "org.camunda.bpm.engine.bpmn.behavior", Level.INFO);

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/behavior/BpmnBehaviorLoggerTest.UnhandledBpmnError.bpmn20.xml" })
  public void shouldIncludeBpmnErrorMessageInUnhandledBpmnError() {
    // given
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(true);
    String errorMessage = "Execution with id 'serviceTask' throws an error event with errorCode 'errorCode' and errorMessage 'ouch!', but no error handler was defined";
    // when & then
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(errorMessage);
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/behavior/BpmnBehaviorLoggerTest.UnhandledBpmnError.bpmn20.xml" })
  public void shouldLogBpmnErrorMessageInUnhandledBpmnErrorWithoutException() {
    // given
    String logMessage = "Execution with id 'serviceTask' throws an error event with errorCode 'errorCode' and errorMessage 'ouch!'";
    // when
    runtimeService.startProcessInstanceByKey("testProcess");
    // then
    assertThat(processEngineLoggingRule.getFilteredLog(logMessage)).hasSize(1);
  }
}
