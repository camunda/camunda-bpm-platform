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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.util.ChangeVariablesDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class JobEntityTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected List<String> jobIds = new ArrayList<>();

  protected HistoryService historyService;
  protected ManagementService managementService;
  protected RuntimeService runtimeService;

  protected static final Date CREATE_DATE = new Date(1363607000000L);

  protected String activityIdLoggingProperty;

  @Before
  public void setUp() {
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();

    jobIds = new ArrayList<>();

    activityIdLoggingProperty = engineRule.getProcessEngineConfiguration().getLoggingContextActivityId();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(CREATE_DATE);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void cleanup() {
    for (String jobId : jobIds) {
      managementService.deleteJob(jobId);
    }

    if (!testRule.isHistoryLevelNone()) {
      cleanupJobLog();
    }

    engineRule.getProcessEngineConfiguration().setLoggingContextActivityId(activityIdLoggingProperty);
  }

  @Test
  public void shouldCheckCreateTimeOnMessage() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
        .camundaAsyncBefore()
      .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey("process");

    // when
    Job messageJob = managementService.createJobQuery().singleResult();

    // then
    assertThat(messageJob.getCreateTime(), is(CREATE_DATE));
    assertThat(messageJob.getClass().getSimpleName(), is("MessageEntity"));

    // cleanup
    jobIds.add(messageJob.getId());
  }

  @Test
  public void shouldCheckCreateTimeOnTimer() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
        .timerWithDuration("PT5S")
      .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey("process");

    // when
    Job timerJob = managementService.createJobQuery().singleResult();

    // then
    assertThat(timerJob.getCreateTime(), is(CREATE_DATE));
    assertThat(timerJob.getClass().getSimpleName(), is("TimerEntity"));

    // cleanup
    jobIds.add(timerJob.getId());
  }

  @Test
  public void shouldCheckCreateTimeOnEverLivingJob() {
    // given
    historyService.cleanUpHistoryAsync(true);

    // when
    Job everLivingJob = managementService.createJobQuery().singleResult();

    // then
    assertThat(everLivingJob.getCreateTime(), is(CREATE_DATE));
    assertThat(everLivingJob.getClass().getSimpleName(), is("EverLivingJobEntity"));

    // cleanup
    jobIds.add(everLivingJob.getId());
  }

  @Test
  public void shouldShowFailedActivityIdPropertyForFailingAsyncTask() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("theTask")
        .camundaAsyncBefore()
        .camundaClass(FailingDelegate.class)
      .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", true));
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();

    // when
    try {
      managementService.executeJob(job.getId());
      fail("Exception expected");
    } catch (Exception e) {
      // exception expected
    }

    // then
    job = (JobEntity) managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getFailedActivityId(), is("theTask"));
  }

  @Test
  public void shouldShowFailedActivityIdIfActivityIdLoggingIsDisabled() {
    // given
    engineRule.getProcessEngineConfiguration().setLoggingContextActivityId(null);

    testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("theTask")
        .camundaAsyncBefore()
        .camundaClass(FailingDelegate.class)
      .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", true));
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();

    // when
    try {
      managementService.executeJob(job.getId());
      fail("Exception expected");
    } catch (Exception e) {
      // exception expected
    }

    // then
    job = (JobEntity) managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getFailedActivityId(), is("theTask"));
  }

  @Test
  public void shouldShowFailedActivityIdPropertyForAsyncTaskWithFailingFollowUp() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("theTask")
        .camundaAsyncBefore()
        .camundaClass(ChangeVariablesDelegate.class)
      .serviceTask("theTask2").camundaClass(ChangeVariablesDelegate.class)
      .serviceTask("theTask3").camundaClass(FailingDelegate.class)
      .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", true));
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();

    // when
    try {
      managementService.executeJob(job.getId());
      fail("Exception expected");
    } catch (Exception e) {
      // exception expected
    }

    // then
    job = (JobEntity) managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getFailedActivityId(), is("theTask3"));
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected void cleanupJobLog() {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          for (String jobId : jobIds) {
            commandContext.getHistoricJobLogManager()
              .deleteHistoricJobLogByJobId(jobId);
          }

          return null;
        }
      });
  }

}
