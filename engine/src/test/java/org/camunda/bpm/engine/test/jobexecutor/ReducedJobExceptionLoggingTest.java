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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.WatchLogger;
import org.camunda.bpm.engine.test.util.ProcessEngineLoggingRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class ReducedJobExceptionLoggingTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(loggingRule);

  private RuntimeService runtimeService;
  private ProcessEngineConfiguration processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setEnableReducedJobExceptionLogging(false);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @WatchLogger(loggerNames = { "org.camunda.bpm.engine.context" }, level = "DEBUG")
  public void shouldLogAllFailingJobExceptions() {
    // given
    processEngineConfiguration.setEnableReducedJobExceptionLogging(false);

    runtimeService.startProcessInstanceByKey("failingProcess");

    // when the job is run several times till the incident creation
    Job job = getJob();
    while (job.getRetries() > 0 && ((JobEntity) job).getLockOwner() == null) {
      try {
        lockTheJob(job.getId());
        engineRule.getManagementService().executeJob(job.getId());
      } catch (Exception ex) {
      }
      job = getJob();
    }

    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Exception while closing command context:");

    // then
    assertThat(filteredLogList.size(), CoreMatchers.is(3));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @WatchLogger(loggerNames = { "org.camunda.bpm.engine.context" }, level = "DEBUG")
  public void shouldLogOnlyOneFailingJobException() {
    // given
    processEngineConfiguration.setEnableReducedJobExceptionLogging(true);

    runtimeService.startProcessInstanceByKey("failingProcess");

    // when the job is run several times till the incident creation
    Job job = getJob();
    while (job.getRetries() > 0 && ((JobEntity) job).getLockOwner() == null) {
      try {
        lockTheJob(job.getId());
        engineRule.getManagementService().executeJob(job.getId());
      } catch (Exception ex) {
      }
      job = getJob();
    }

    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Exception while closing command context:");

    // then
    assertThat(filteredLogList.size(), CoreMatchers.is(1));
  }

  private void lockTheJob(final String jobId) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        final JobEntity job = commandContext.getJobManager().findJobById(jobId);
        job.setLockOwner("someLockOwner");
        job.setLockExpirationTime(DateUtils.addHours(ClockUtil.getCurrentTime(), 1));
        return null;
      }
    });
  }

  private Job getJob() {
    List<Job> jobs = engineRule.getManagementService().createJobQuery().list();
    assertEquals(1, jobs.size());
    return jobs.get(0);
  }
}
