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
package org.camunda.bpm.qa.rolling.update.batch;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.rolling.update.RollingUpdateConstants;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Before;
import org.junit.Test;

@ScenarioUnderTest("SetRemovalTimeToProcessInstanceScenario")
public class SetRemovalTimeToProcessInstanceTest extends AbstractRollingUpdateTestCase {

  protected ManagementService managementService;
  protected RuntimeService runtimeService;

  @Before
  public void setUp() {
    managementService = rule.getManagementService();
    runtimeService = rule.getRuntimeService();
  }

  @Test
  @ScenarioUnderTest("createSetRemovalTimeToProcessInstanceBatch.1")
  public void shouldCompleteBatch() {
    if (RollingUpdateConstants.OLD_ENGINE_TAG.equals(rule.getTag())) { // test cleanup with old engine
      Date removalTime = new Date(1363609000000L);

      String processInstanceId = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("SetRemovalTimeToProcessInstance.batch").singleResult().getId();
      String batchId = managementService.getProperties().get("SetRemovalTimeToProcessInstance.batch.batchId");
      Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
      String seedJobDefinitionId = batch.getSeedJobDefinitionId();
      Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();

      managementService.executeJob(seedJob.getId());
      List<Job> batchJobs = managementService.createJobQuery()
          .jobDefinitionId(batch.getBatchJobDefinitionId())
          .list();

      // when
      batchJobs.forEach(job -> managementService.executeJob(job.getId()));

      // then
      HistoricActivityInstance historicActivityInstance = rule.getHistoryService().createHistoricActivityInstanceQuery()
          .activityId("theTask")
          .processInstanceId(processInstanceId)
          .singleResult();
      assertEquals(removalTime, historicActivityInstance.getRemovalTime());

      assertEquals(0, managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).count());
    }
  }

  @Test
  @ScenarioUnderTest("createSetRemovalTimeToProcessInstanceBatchJob.1")
  public void testCompleteBatchKJob() {
    if (RollingUpdateConstants.OLD_ENGINE_TAG.equals(rule.getTag())) { // test cleanup with old engine
      Date removalTime = new Date(1363609000000L);

      String processInstanceId = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("SetRemovalTimeToProcessInstance.batchJob").singleResult().getId();
      String batchId = managementService.getProperties().get("SetRemovalTimeToProcessInstance.batchJob.batchId");
      Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
      List<Job> batchJobs = managementService.createJobQuery()
          .jobDefinitionId(batch.getBatchJobDefinitionId())
          .list();

      // when
      batchJobs.forEach(job -> managementService.executeJob(job.getId()));

      // then
      HistoricActivityInstance historicActivityInstance = rule.getHistoryService().createHistoricActivityInstanceQuery()
          .activityId("theTask")
          .processInstanceId(processInstanceId)
          .singleResult();
      assertEquals(removalTime, historicActivityInstance.getRemovalTime());

      assertEquals(0, managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).count());
    }
  }

}
