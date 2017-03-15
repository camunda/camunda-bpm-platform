/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.runtime.migration.batch;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.BatchHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;

public class BatchMigrationHelper extends BatchHelper{

  protected MigrationTestRule migrationRule;

  public ProcessDefinition sourceProcessDefinition;
  public ProcessDefinition targetProcessDefinition;

  public BatchMigrationHelper(ProcessEngineRule engineRule, MigrationTestRule migrationRule) {
    super(engineRule);
    this.migrationRule = migrationRule;
  }

  public BatchMigrationHelper(ProcessEngineRule engineRule) {
    this(engineRule, null);
  }

  public ProcessDefinition getSourceProcessDefinition() {
    return sourceProcessDefinition;
  }

  public ProcessDefinition getTargetProcessDefinition() {
    return targetProcessDefinition;
  }

  public Batch createMigrationBatchWithSize(int batchSize) {
    int invocationsPerBatchJob = ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration()).getInvocationsPerBatchJob();
    return migrateProcessInstancesAsync(invocationsPerBatchJob * batchSize);
  }

  public Batch migrateProcessInstancesAsync(int numberOfProcessInstances) {
    sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    targetProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    return migrateProcessInstancesAsync(numberOfProcessInstances, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstancesAsyncForTenant(int numberOfProcessInstances, String tenantId) {
    sourceProcessDefinition = migrationRule.deployForTenantAndGetDefinition(tenantId, ProcessModels.ONE_TASK_PROCESS);
    targetProcessDefinition = migrationRule.deployForTenantAndGetDefinition(tenantId, ProcessModels.ONE_TASK_PROCESS);
    return migrateProcessInstancesAsync(numberOfProcessInstances, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstanceAsync(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    return migrateProcessInstancesAsync(1, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstancesAsync(int numberOfProcessInstances, ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = new ArrayList<String>(numberOfProcessInstances);
    for (int i = 0; i < numberOfProcessInstances; i++) {
      processInstanceIds.add(
        runtimeService.startProcessInstanceById(sourceProcessDefinition.getId()).getId());
    }

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    return runtimeService.newMigration(migrationPlan).processInstanceIds(processInstanceIds).executeAsync();
  }

  @Override
  public JobDefinition getExecutionJobDefinition(Batch batch) {
    return engineRule.getManagementService()
      .createJobDefinitionQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).jobType(Batch.TYPE_PROCESS_INSTANCE_MIGRATION).singleResult();
  }

  public long countSourceProcessInstances() {
    return engineRule.getRuntimeService()
      .createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId()).count();
  }

  public long countTargetProcessInstances() {
    return engineRule.getRuntimeService()
      .createProcessInstanceQuery().processDefinitionId(targetProcessDefinition.getId()).count();
  }
}
