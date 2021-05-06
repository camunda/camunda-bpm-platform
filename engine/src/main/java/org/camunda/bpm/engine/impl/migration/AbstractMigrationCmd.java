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
package org.camunda.bpm.engine.impl.migration;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractMigrationCmd {

  protected MigrationPlanExecutionBuilderImpl executionBuilder;

  public AbstractMigrationCmd(MigrationPlanExecutionBuilderImpl executionBuilder) {
    this.executionBuilder = executionBuilder;
  }

  protected void checkAuthorizations(CommandContext commandContext,
                                     ProcessDefinitionEntity sourceDefinition,
                                     ProcessDefinitionEntity targetDefinition) {

    CompositePermissionCheck migrateInstanceCheck = new PermissionCheckBuilder()
      .conjunctive()
      .atomicCheckForResourceId(Resources.PROCESS_DEFINITION,
          sourceDefinition.getKey(), Permissions.MIGRATE_INSTANCE)
      .atomicCheckForResourceId(Resources.PROCESS_DEFINITION,
          targetDefinition.getKey(), Permissions.MIGRATE_INSTANCE)
    .build();

    commandContext.getAuthorizationManager().checkAuthorization(migrateInstanceCheck);
  }

  protected Collection<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<>();

    List<String> processInstanceIds = executionBuilder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final ProcessInstanceQueryImpl processInstanceQuery =
        (ProcessInstanceQueryImpl) executionBuilder.getProcessInstanceQuery();
    if (processInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

    return collectedProcessInstanceIds;
  }

  protected void writeUserOperationLog(CommandContext commandContext,
      ProcessDefinitionEntity sourceProcessDefinition,
      ProcessDefinitionEntity targetProcessDefinition,
      int numInstances,
      Map<String, Object> variables,
      boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("processDefinitionId",
        sourceProcessDefinition.getId(),
        targetProcessDefinition.getId()));
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));

    if (variables != null) {
      propertyChanges.add(new PropertyChange("nrOfSetVariables", null, variables.size()));
    }
    propertyChanges.add(new PropertyChange("async", null, async));

    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_MIGRATE,
          null,
          sourceProcessDefinition.getId(),
          sourceProcessDefinition.getKey(),
          propertyChanges);
  }

  protected ProcessDefinitionEntity resolveSourceProcessDefinition(CommandContext commandContext) {

    String sourceProcessDefinitionId = executionBuilder.getMigrationPlan()
        .getSourceProcessDefinitionId();

    ProcessDefinitionEntity sourceProcessDefinition =
        getProcessDefinition(commandContext, sourceProcessDefinitionId);
    EnsureUtil.ensureNotNull("sourceProcessDefinition", sourceProcessDefinition);

    return sourceProcessDefinition;
  }

  protected ProcessDefinitionEntity resolveTargetProcessDefinition(CommandContext commandContext) {
    String targetProcessDefinitionId = executionBuilder.getMigrationPlan()
        .getTargetProcessDefinitionId();

    ProcessDefinitionEntity sourceProcessDefinition =
        getProcessDefinition(commandContext, targetProcessDefinitionId);
    EnsureUtil.ensureNotNull("sourceProcessDefinition", sourceProcessDefinition);

    return sourceProcessDefinition;
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext,
                                                         String processDefinitionId) {

    return commandContext
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }
}
