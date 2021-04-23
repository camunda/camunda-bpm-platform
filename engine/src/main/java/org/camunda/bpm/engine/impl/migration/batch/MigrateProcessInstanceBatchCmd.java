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
package org.camunda.bpm.engine.impl.migration.batch;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMapping;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.core.variable.VariableUtil;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.AbstractMigrationCmd;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.migration.MigrationPlan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class MigrateProcessInstanceBatchCmd extends AbstractMigrationCmd implements Command<Batch> {

  public MigrateProcessInstanceBatchCmd(MigrationPlanExecutionBuilderImpl builder) {
    super(builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    Collection<String> collectedInstanceIds = collectProcessInstanceIds();

    MigrationPlan migrationPlan = executionBuilder.getMigrationPlan();

    ensureNotNull(BadUserRequestException.class,
        "Migration plan cannot be null", "migration plan", migrationPlan);

    ensureNotEmpty(BadUserRequestException.class,
        "Process instance ids cannot empty", "process instance ids", collectedInstanceIds);

    ensureNotContainsNull(BadUserRequestException.class,
        "Process instance ids cannot be null", "process instance ids", collectedInstanceIds);

    ProcessDefinitionEntity sourceDefinition = resolveSourceProcessDefinition(commandContext);
    ProcessDefinitionEntity targetDefinition = resolveTargetProcessDefinition(commandContext);

    String tenantId = sourceDefinition.getTenantId();

    Map<String, Object> variables = migrationPlan.getVariables();

    Batch batch = new BatchBuilder(commandContext)
        .type(Batch.TYPE_PROCESS_INSTANCE_MIGRATION)
        .config(getConfiguration(collectedInstanceIds, sourceDefinition.getDeploymentId()))
        .permission(BatchPermissions.CREATE_BATCH_MIGRATE_PROCESS_INSTANCES)
        .permissionHandler(ctx -> checkAuthorizations(ctx, sourceDefinition, targetDefinition))
        .tenantId(tenantId)
        .operationLogHandler((ctx, instanceCount) ->
            writeUserOperationLog(ctx, sourceDefinition, targetDefinition, instanceCount, variables, true))
        .build();

    if (variables != null) {
      String batchId = batch.getId();
      VariableUtil.setVariablesByBatchId(variables, batchId);
    }

    return batch;
  }

  public BatchConfiguration getConfiguration(Collection<String> instanceIds, String deploymentId) {
    return new MigrationBatchConfiguration(
        new ArrayList<>(instanceIds), DeploymentMappings.of(new DeploymentMapping(deploymentId, instanceIds.size())),
        executionBuilder.getMigrationPlan(),
        executionBuilder.isSkipCustomListeners(),
        executionBuilder.isSkipIoMappings());
  }

}
