/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration.batch;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BatchMigrationUserOperationLogTest {

  public static final String USER_ID = "userId";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);

  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  @After
  public void removeBatches() {
    batchHelper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void testLogCreation() {
    // given
    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    MigrationPlan migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    engineRule.getIdentityService().setAuthenticatedUserId(USER_ID);
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();
    engineRule.getIdentityService().clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = engineRule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(3, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry procDefEntry = entries.get("processDefinitionId");
    Assert.assertNotNull(procDefEntry);
    Assert.assertEquals("ProcessInstance", procDefEntry.getEntityType());
    Assert.assertEquals("Migrate", procDefEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), procDefEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), procDefEntry.getProcessDefinitionKey());
    Assert.assertNull(procDefEntry.getProcessInstanceId());
    Assert.assertEquals(sourceProcessDefinition.getId(), procDefEntry.getOrgValue());
    Assert.assertEquals(targetProcessDefinition.getId(), procDefEntry.getNewValue());

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("Migrate", asyncEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("true", asyncEntry.getNewValue());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals("ProcessInstance", numInstancesEntry.getEntityType());
    Assert.assertEquals("Migrate", numInstancesEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), numInstancesEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("1", numInstancesEntry.getNewValue());

    Assert.assertEquals(procDefEntry.getOperationId(), asyncEntry.getOperationId());
    Assert.assertEquals(asyncEntry.getOperationId(), numInstancesEntry.getOperationId());
  }

  @Test
  public void testNoCreationOnSyncBatchJobExecution() {
    // given
    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    MigrationPlan migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    Batch batch = engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();
    batchHelper.executeSeedJob(batch);

    // when
    engineRule.getIdentityService().setAuthenticatedUserId(USER_ID);
    batchHelper.executeJobs(batch);
    engineRule.getIdentityService().clearAuthentication();

    // then
    Assert.assertEquals(0, engineRule.getHistoryService().createUserOperationLogQuery().count());
  }

  @Test
  public void testNoCreationOnJobExecutorBatchJobExecution() {
    // given
    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    MigrationPlan migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    // when
    migrationRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    Assert.assertEquals(0, engineRule.getHistoryService().createUserOperationLogQuery().count());
  }

  protected Map<String, UserOperationLogEntry> asMap(List<UserOperationLogEntry> logEntries) {
    Map<String, UserOperationLogEntry> map = new HashMap<String, UserOperationLogEntry>();

    for (UserOperationLogEntry entry : logEntries) {

      UserOperationLogEntry previousValue = map.put(entry.getProperty(), entry);
      if (previousValue != null) {
        Assert.fail("expected only entry for every property");
      }
    }

    return map;
  }
}
