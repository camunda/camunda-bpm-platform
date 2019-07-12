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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineLoggingRule;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class JobExceptionLoggingTest {

  private static final String JOBEXECUTOR_LOGGER = "org.camunda.bpm.engine.jobexecutor";
  private static final String CONTEXT_LOGGER = "org.camunda.bpm.engine.context";

  public ProcessEngineRule engineRule = new ProcessEngineRule();
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch(CONTEXT_LOGGER, JOBEXECUTOR_LOGGER).level(Level.DEBUG);
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(loggingRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  protected TweetNestedCommandExceptionHandler cmdExceptionHandler = new TweetNestedCommandExceptionHandler();
  
  @Before
  public void init() {
    runtimeService = engineRule.getProcessEngine().getRuntimeService();
    managementService = engineRule.getProcessEngine().getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration.setDefaultNumberOfRetries(1);
    processEngineConfiguration.getJobHandlers().put(cmdExceptionHandler.getType(), cmdExceptionHandler);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setDefaultNumberOfRetries(3);
    processEngineConfiguration.setEnableCmdExceptionLogging(false);
    processEngineConfiguration.getJobHandlers().remove(cmdExceptionHandler.getType());
    List<Job> jobs = managementService.createJobQuery().processDefinitionKey("testProcess").list();
    for (Job job : jobs) {
      managementService.deleteJob(job.getId());
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/delegateThrowsException.bpmn20.xml")
  public void shouldLogFailingJobOnlyOnce() {
    // given a job that always throws an Exception
    processEngineConfiguration.setEnableCmdExceptionLogging(false);
    runtimeService.startProcessInstanceByKey("testProcess");

    // when executing the job and wait
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
    }

    List<ILoggingEvent> jobLog = loggingRule.getFilteredLog(JOBEXECUTOR_LOGGER, "Exception while executing job");
    List<ILoggingEvent> ctxLog = loggingRule.getFilteredLog(CONTEXT_LOGGER, "Exception while closing command context");

    // then
    assertThat(jobLog.size(), is(1));
    assertThat(ctxLog.size(), is(0));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/delegateThrowsException.bpmn20.xml")
  public void shouldLogFailingJobTwice() {
    // given a job that always throws an Exception
    processEngineConfiguration.setEnableCmdExceptionLogging(true);
    runtimeService.startProcessInstanceByKey("testProcess");
    
    // when executing the job and wait
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
    }

    List<ILoggingEvent> jobLog = loggingRule.getFilteredLog(JOBEXECUTOR_LOGGER, "Exception while executing job");
    List<ILoggingEvent> ctxLog = loggingRule.getFilteredLog(CONTEXT_LOGGER, "Exception while closing command context");
    
    // then
    assertThat(jobLog.size(), is(1));
    assertThat(ctxLog.size(), is(1));
  }

  @Test
  public void shouldNotLogExceptionWhenApiCall() {
    // given
    String jobId = processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        MessageEntity message = new MessageEntity();
        message.setJobHandlerType(TweetNestedCommandExceptionHandler.TYPE);
        commandContext.getJobManager().insertJob(message);
        return message.getId();
      }
    });

    // when
    RuntimeException expectedException = null;
    try {
      managementService.executeJob(jobId);
    } catch (RuntimeException e) {
      expectedException = e;
    }
    List<ILoggingEvent> jobLog = loggingRule.getFilteredLog(JOBEXECUTOR_LOGGER, "Exception while executing job");
    List<ILoggingEvent> ctxLog = loggingRule.getFilteredLog(CONTEXT_LOGGER, "Exception while closing command context");

    // then
    // make sure the exceptions is thrown...
    assertNotNull(expectedException);
    assertThat(expectedException.getMessage(), Matchers.containsString("nested command exception"));
    // ...but not logged
    assertThat(jobLog.size(), is(0));
    assertThat(ctxLog.size(), is(0));

    // clean
    managementService.deleteJob(jobId);
  }
}
