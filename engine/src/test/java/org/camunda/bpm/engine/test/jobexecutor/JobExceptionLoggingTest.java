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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collection;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JobExceptionLoggingTest {

  ProcessEngineConfiguration processEngineConfigurationDefaultLogging = new StandaloneProcessEngineConfiguration();
  ProcessEngineConfiguration processEngineConfigurationReducedJobLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedJobExceptionLogging(true);
  ProcessEngineConfiguration processEngineConfigurationReducedCmdLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedCmdExceptionLogging(true);
  ProcessEngineConfiguration processEngineConfigurationReducedJobAndCmdLogging = new StandaloneProcessEngineConfiguration()
      .setEnableReducedJobExceptionLogging(true)
      .setEnableReducedCmdExceptionLogging(true);

  @Parameter(0)
  public JobEntity job;

  @Parameter(1)
  public boolean expectedDefaultLoggingResult;

  @Parameter(2)
  public boolean expectedJobExceptionLogging;

  @Parameter(3)
  public boolean expectedCmdExceptionLogging;

  @Parameter(4)
  public boolean expectedJobAndCmdExceptionLogging;

  @Parameters(name = "scenario {index}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      {jobWithRetries(3), true, false, true, false},
      {jobWithRetries(1), true, true, true, true},
      {null, true, true, true, true}
    });
  }

  @Test
  public void testLogJobException() {
    // given parameters

    // when
    boolean shouldLogWithDefault = ProcessEngineLogger.shouldLogJobException(processEngineConfigurationDefaultLogging, job);
    boolean shouldLogWithReducedJobLogging = ProcessEngineLogger.shouldLogJobException(processEngineConfigurationReducedJobLogging, job);
    boolean shouldLogWithReducedCmdLogging = ProcessEngineLogger.shouldLogJobException(processEngineConfigurationReducedCmdLogging, job);
    boolean shouldLogWithReducedJobAndCmdLogging = ProcessEngineLogger.shouldLogJobException(processEngineConfigurationReducedJobAndCmdLogging, job);

    // then
    assertThat(shouldLogWithDefault, is(expectedDefaultLoggingResult));
    assertThat(shouldLogWithReducedJobLogging, is(expectedJobExceptionLogging));
    assertThat(shouldLogWithReducedCmdLogging, is(expectedCmdExceptionLogging));
    assertThat(shouldLogWithReducedJobAndCmdLogging, is(expectedJobAndCmdExceptionLogging));
  }

  private static JobEntity jobWithRetries(int retries) {
    JobEntity job = new TimerEntity();
    job.setRetries(retries);
    return job;
  }
}
