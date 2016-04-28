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
package org.camunda.bpm.engine.impl.migration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractMigrationCmd<T> implements Command<T> {

  protected MigrationPlanExecutionBuilderImpl executionBuilder;

  public AbstractMigrationCmd(MigrationPlanExecutionBuilderImpl executionBuilder) {
    this.executionBuilder = executionBuilder;
  }

  protected void checkAuthorizations(CommandContext commandContext,
      ProcessDefinitionEntity sourceDefinition,
      ProcessDefinitionEntity targetDefinition,
      Collection<String> processInstanceIds) {

    CompositePermissionCheck migrateInstanceCheck = new PermissionCheckBuilder()
      .conjunctive()
      .atomicCheckForResourceId(Resources.PROCESS_DEFINITION, sourceDefinition.getKey(), Permissions.MIGRATE_INSTANCE)
      .atomicCheckForResourceId(Resources.PROCESS_DEFINITION, targetDefinition.getKey(), Permissions.MIGRATE_INSTANCE)
    .build();

    commandContext.getAuthorizationManager().checkAuthorization(migrateInstanceCheck);
  }

  protected Collection<String> collectProcessInstanceIds(CommandContext commandContext) {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = executionBuilder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) executionBuilder.getProcessInstanceQuery();
    if (processInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

    return collectedProcessInstanceIds;
  }

  protected void checkRequiredAuthorizations(CommandContext commandContext, MigrationPlan migrationPlan, Collection<String> processInstanceIds) {
    ProcessDefinitionEntity sourceProcessDefinition = commandContext.getProcessEngineConfiguration()
      .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getSourceProcessDefinitionId());
    ProcessDefinitionEntity targetProcessDefinition = commandContext.getProcessEngineConfiguration()
      .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());

    EnsureUtil.ensureNotNull("sourceProcessDefinition", sourceProcessDefinition);
    EnsureUtil.ensureNotNull("targetProcessDefinition", targetProcessDefinition);
    checkAuthorizations(commandContext, sourceProcessDefinition, targetProcessDefinition, processInstanceIds);
  }
}
