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
package org.camunda.bpm.qa.upgrade.scenarios7130.batch.deploymentaware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("DeploymentAwareBatchesScenario")
@Origin("7.12.0")
public class LegacyDeploymentAwarenessTest {

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
  }

  @Test
  @ScenarioUnderTest("createDeleteInstancesBatch.1")
  public void shouldCreateDeploymentAwareDeleteBatchJobs() {
    // given
    Map<String, String> properties = managementService.getProperties();

    String batchId = properties.get(getPropertyName("delete.batchId"));
    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    assertNotNull(batch);

    // when
    Job seedJob = getSeedJob(batch);
    assertNull(seedJob.getDeploymentId());
    executeSeedJob(batch);
    assertNull(getSeedJob(batch));

    // then
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertEquals(4, batchJobs.size());

    assertEquals(2L, getJobCountByDeployment(batchJobs, properties.get(getPropertyName("deploymentId1"))));
    assertEquals(2L, getJobCountByDeployment(batchJobs, properties.get(getPropertyName("deploymentId2"))));
  }

  @Test
  @ScenarioUnderTest("createRestartInstancesBatch.1")
  public void shouldCreateDeploymentAwareRestartBatchJobs() {
    // given
    Map<String, String> properties = managementService.getProperties();

    String batchId = properties.get(getPropertyName("restart.batchId"));
    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    assertNotNull(batch);

    // when
    Job seedJob = getSeedJob(batch);
    assertNull(seedJob.getDeploymentId());
    executeSeedJob(batch);
    assertNull(getSeedJob(batch));

    // then
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertEquals(2, batchJobs.size());

    assertEquals(2L, getJobCountByDeployment(batchJobs, properties.get(getPropertyName("deploymentId2"))));
  }

  @Test
  @ScenarioUnderTest("createModificationInstancesBatch.1")
  public void shouldCreateDeploymentAwareModificationBatchJobs() {
    // given
    Map<String, String> properties = managementService.getProperties();

    String batchId = properties.get(getPropertyName("modify.batchId"));
    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    assertNotNull(batch);

    // when
    Job seedJob = getSeedJob(batch);
    assertNull(seedJob.getDeploymentId());
    executeSeedJob(batch);
    assertNull(getSeedJob(batch));

    // then
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertEquals(1, batchJobs.size());

    assertEquals(1L, getJobCountByDeployment(batchJobs, properties.get(getPropertyName("deploymentId2"))));
  }

  @Test
  @ScenarioUnderTest("createMigrationInstancesBatch.1")
  public void shouldCreateDeploymentAwareMigrationBatchJobs() {
    // given
    Map<String, String> properties = managementService.getProperties();

    String batchId = properties.get(getPropertyName("migrate.batchId"));
    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    assertNotNull(batch);

    // when
    Job seedJob = getSeedJob(batch);
    assertNull(seedJob.getDeploymentId());
    executeSeedJob(batch);
    assertNull(getSeedJob(batch));

    // then
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertEquals(1, batchJobs.size());

    assertEquals(1L, getJobCountByDeployment(batchJobs, properties.get(getPropertyName("deploymentId1"))));
  }

  protected static String getPropertyName(String suffix) {
    return "DeploymentAwareBatches." + suffix;
  }

  protected void executeSeedJobs(Batch batch, int expectedSeedJobsCount) {
    for (int i = 0; i < expectedSeedJobsCount; i++) {
      executeSeedJob(batch);
    }
    assertNull(getSeedJob(batch));
  }

  protected Job getSeedJob(Batch batch) {
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
    return seedJob;
  }

  protected void executeSeedJob(Batch batch) {
    Job seedJob = getSeedJob(batch);
    assertNotNull(seedJob);
    managementService.executeJob(seedJob.getId());
  }

  protected long getJobCountByDeployment(List<Job> jobs, String deploymentId) {
    return jobs.stream().filter(j -> deploymentId.equals(j.getDeploymentId())).count();
  }
}
