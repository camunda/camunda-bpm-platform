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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.DefaultPriorityProvider;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HistoryCleanupJobPriorityTest {

  private static final Long CUSTOM_PRIORITY = 10L;

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl config;
  protected HistoryService historyService;

  protected long defaultHistoryCleanupJobPriority;

  @Before
  public void setup() {
    config = engineRule.getProcessEngineConfiguration();
    historyService = engineRule.getHistoryService();
    defaultHistoryCleanupJobPriority = config.getHistoryCleanupJobPriority();
  }

  @After
  public void reset() {
    config.setHistoryCleanupJobPriority(defaultHistoryCleanupJobPriority);
    resetDatabase();
  }

  private void resetDatabase() {
    config.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        List<Job> jobs = historyService.findHistoryCleanupJobs();

        for (Job job : jobs) {
          commandContext.getJobManager().deleteJob((JobEntity) job);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        }
        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

  @Test
  public void historyCleanupJobShouldHaveDefaultPriority() {
    // when
    historyService.cleanUpHistoryAsync(true);
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();

    // then
    assertThat(historyCleanupJobs).hasSize(1);
    assertThat(historyCleanupJobs.get(0).getPriority()).isEqualTo(DefaultPriorityProvider.DEFAULT_PRIORITY);
  }

  @Test
  public void historyCleanupJobShouldGetPriorityFromProcessEngineConfiguration() {
    // given
    config.setHistoryCleanupJobPriority(CUSTOM_PRIORITY);

    // when
    historyService.cleanUpHistoryAsync(true);
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();

    // then
    assertThat(historyCleanupJobs).hasSize(1);
    assertThat(historyCleanupJobs.get(0).getPriority()).isEqualTo(CUSTOM_PRIORITY);
  }
}
