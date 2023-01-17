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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HistoryCleanupJobPriorityRangeTest extends AbstractJobExecutorAcquireJobsTest {

  protected HistoryService historyService;
  protected long defaultHistoryCleanupJobPriority;
  protected boolean defaultIsJobExecutorAcquireByPriority;

  @Before
  public void setup() {
    historyService = rule.getHistoryService();
    defaultHistoryCleanupJobPriority = configuration.getHistoryCleanupJobPriority();
    defaultIsJobExecutorAcquireByPriority = configuration.isJobExecutorAcquireByPriority();
  }

  @After
  public void tearDown() {
    configuration.setHistoryCleanupJobPriority(defaultHistoryCleanupJobPriority);
    configuration.setJobExecutorAcquireByPriority(defaultIsJobExecutorAcquireByPriority);
    resetDatabase();
  }

  private void resetDatabase() {
    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {
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
  public void shouldSetConfiguredPriorityOnHistoryCleanupJob() {
    // given
    configuration.setHistoryCleanupJobPriority(10L);

    // when
    historyService.cleanUpHistoryAsync(true);
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();

    // then
    assertThat(historyCleanupJobs).hasSize(1);
    assertThat(historyCleanupJobs.get(0).getPriority()).isEqualTo(10L);
  }

  @Test
  public void shouldAcquireHistoryCleanupJobInPriorityRange() {
    // given
    configuration.setJobExecutorPriorityRangeMin(5L);
    configuration.setJobExecutorPriorityRangeMax(15L);
    configuration.setHistoryCleanupJobPriority(10L);

    // when
    historyService.cleanUpHistoryAsync(true);

    // then
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();
    assertThat(acquirableJobs).hasSize(1);
    assertThat(findJobById(acquirableJobs.get(0).getId()).getPriority()).isEqualTo(10L);
  }

  @Test
  public void shouldNotAcquireHistoryCleanupJobOutsidePriorityRange() {
    // given
    configuration.setJobExecutorAcquireByPriority(true);
    configuration.setJobExecutorPriorityRangeMin(5L);
    configuration.setJobExecutorPriorityRangeMax(15L);
    configuration.setHistoryCleanupJobPriority(20L);

    // when
    historyService.cleanUpHistoryAsync(true);

    // then
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();
    assertThat(acquirableJobs).hasSize(0);
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    assertThat(historyCleanupJobs).hasSize(1);
    assertThat(historyCleanupJobs.get(0).getPriority()).isEqualTo(20L);
  }
}
