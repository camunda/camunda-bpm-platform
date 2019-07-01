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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.junit.Test;

public class CmdExceptionLoggingTest {

  ProcessEngineConfiguration processEngineConfigurationDefaultLogging = new StandaloneProcessEngineConfiguration();
  ProcessEngineConfiguration processEngineConfigurationReducedJobLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedJobExceptionLogging(true);
  ProcessEngineConfiguration processEngineConfigurationReducedCmdLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedCmdExceptionLogging(true);
  ProcessEngineConfiguration processEngineConfigurationReducedJobAndCmdLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedJobExceptionLogging(true)
      .setEnableReducedCmdExceptionLogging(true);

  @Test
  public void testLogCmdException() {
    // given different engine configurations

    // when
    boolean shouldLogWithDefault = ProcessEngineLogger.shouldLogCmdException(processEngineConfigurationDefaultLogging);
    boolean shouldLogWithReducedJobLogging = ProcessEngineLogger.shouldLogCmdException(processEngineConfigurationReducedJobLogging);
    boolean shouldLogWithReducedCmdLogging = ProcessEngineLogger.shouldLogCmdException(processEngineConfigurationReducedCmdLogging);
    boolean shouldLogWithReducedJobAndCmdLogging = ProcessEngineLogger.shouldLogCmdException(processEngineConfigurationReducedJobAndCmdLogging);

    // then
    assertTrue(shouldLogWithDefault);
    assertTrue(shouldLogWithReducedJobLogging);
    assertFalse(shouldLogWithReducedCmdLogging);
    assertFalse(shouldLogWithReducedJobAndCmdLogging);
  }
}
