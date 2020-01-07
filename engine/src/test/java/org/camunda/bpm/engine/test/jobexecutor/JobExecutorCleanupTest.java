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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JobExecutorCleanupTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(thrown);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ProcessEngineConfigurationImpl configuration;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    configuration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void resetConfig() {
    configuration.setHistoryCleanupEnabled(true);
  }

  @Test
  public void shouldNotExecuteCleanupJob() {
    // given
    historyService.cleanUpHistoryAsync(true); // schedule cleanup job
    configuration.setHistoryCleanupEnabled(false);

    // then: job cannot be acquired & executed
    thrown.expect(AssertionError.class);
    thrown.expectMessage("time limit of 10000 was exceeded");

    // when: execute cleanup job
    testRule.waitForJobExecutorToProcessAllJobs();
  }

  @After
  public void resetDatabase() {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        String handlerType = "history-cleanup";
        List<Job> jobsByHandlerType = commandContext.getJobManager()
            .findJobsByHandlerType(handlerType);

        for (Job job : jobsByHandlerType) {
          commandContext.getJobManager()
              .deleteJob((JobEntity) job);
        }

        commandContext.getHistoricJobLogManager()
            .deleteHistoricJobLogsByHandlerType(handlerType);

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

}
