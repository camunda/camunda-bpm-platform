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

package org.camunda.bpm.engine.test.api.history.removaltime.cleanup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHandler.MAX_BATCH_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.DefaultHistoryRemovalTimeProvider;
import org.camunda.bpm.engine.impl.history.event.HistoricJobLogEvent;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupRemovalTime;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.EntityRemoveRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.RemoveAfter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(HISTORY_FULL)
public class HistoryCleanupByteArrayRemovalTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected EntityRemoveRule entityRemoveRule = EntityRemoveRule.of(testRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule)
      .around(testRule)
      .around(entityRemoveRule);

  private ManagementService managementService;
  private HistoryService historyService;

  private ProcessEngineConfigurationImpl engineConfiguration;

  @Before
  public void init() {
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();

    initEngineConfiguration();
  }

  @After
  public void tearDown() {
    restoreCleanupJobHandler();
    runHistoryCleanup();
  }

  @Test
  @RemoveAfter
  public void shouldHaveRemovalTimeOnFailingHistoryCleanupJob() {
    // given
    engineConfiguration.setHistoryCleanupJobLogTimeToLive("1");
    overrideFailingCleanupJobHandler();

    try {
      // when
      runHistoryCleanup();
      fail("This test should fail during history cleanup and not reach this point");
    } catch (Exception e) {
      HistoricJobLogEvent event = getLastFailingHistoryCleanupJobEvent();
      String exceptionByteArrayId = event.getExceptionByteArrayId();
      ByteArrayEntity byteArray = findByteArrayById(exceptionByteArrayId);

      // then
      assertThat(byteArray).isNotNull();
      assertThat(byteArray.getRemovalTime()).isNotNull();
    }
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId) {
    return engineConfiguration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(byteArrayId));
  }

  protected void restoreCleanupJobHandler() {
    engineConfiguration.getJobHandlers().put(HistoryCleanupJobHandler.TYPE, new HistoryCleanupJobHandler());
  }

  protected void overrideFailingCleanupJobHandler() {
    engineConfiguration.getJobHandlers().put(HistoryCleanupJobHandler.TYPE, new FailingHistoryCleanupJobHandler());
  }

  protected List<Job> runHistoryCleanup() {
    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();
    List<String> jobIds = new ArrayList<>();

    for (Job job : jobs) {
      jobIds.add(job.getId());
      managementService.executeJob(job.getId());
    }

    return jobs;
  }

  protected HistoricJobLogEvent getLastFailingHistoryCleanupJobEvent() {
    return (HistoricJobLogEvent) historyService.createHistoricJobLogQuery()
        .failureLog()
        .jobDefinitionType("history-cleanup")
        .singleResult();
  }

  protected void initEngineConfiguration() {
    engineConfiguration = engineRule.getProcessEngineConfiguration();

    engineConfiguration.setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
        .setHistoryRemovalTimeProvider(new DefaultHistoryRemovalTimeProvider())
        .initHistoryRemovalTime();

    engineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);

    engineConfiguration.setHistoryCleanupBatchSize(MAX_BATCH_SIZE);
    engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    engineConfiguration.setHistoryCleanupDegreeOfParallelism(1);

    engineConfiguration.setBatchOperationHistoryTimeToLive(null);
    engineConfiguration.setBatchOperationsForHistoryCleanup(null);

    engineConfiguration.setHistoryTimeToLive(null);

    engineConfiguration.setTaskMetricsEnabled(false);
    engineConfiguration.setTaskMetricsTimeToLive(null);

    engineConfiguration.initHistoryCleanup();
  }

  /* History Cleanup Job Handler that fails during process cleanup */
  static class FailingHistoryCleanupJobHandler extends HistoryCleanupJobHandler {

    @Override
    protected HistoryCleanupRemovalTime getTimeBasedHandler() {
      return new FailingProcessCleanupRemovalTime();
    }

    static class FailingProcessCleanupRemovalTime extends HistoryCleanupRemovalTime {
      @Override
      protected Map<Class<? extends DbEntity>, DbOperation> performProcessCleanup() {
        throw new RuntimeException("This operation is always failing!");
      }
    }
  }
}
