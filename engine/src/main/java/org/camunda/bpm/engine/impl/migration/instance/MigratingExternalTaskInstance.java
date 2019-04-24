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
package org.camunda.bpm.engine.impl.migration.instance;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingExternalTaskInstance implements MigratingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected ExternalTaskEntity externalTask;
  protected MigratingActivityInstance migratingActivityInstance;

  protected List<MigratingInstance> dependentInstances = new ArrayList<MigratingInstance>();

  public MigratingExternalTaskInstance(ExternalTaskEntity externalTask, MigratingActivityInstance migratingActivityInstance) {
    this.externalTask = externalTask;
    this.migratingActivityInstance = migratingActivityInstance;
  }

  @Override
  public void migrateDependentEntities() {
    for (MigratingInstance migratingDependentInstance : dependentInstances) {
      migratingDependentInstance.migrateState();
    }
  }

  @Override
  public boolean isDetached() {
    return externalTask.getExecutionId() == null;
  }

  @Override
  public void detachState() {
    externalTask.getExecution().removeExternalTask(externalTask);
    externalTask.setExecution(null);
  }

  @Override
  public void attachState(MigratingScopeInstance owningInstance) {
    ExecutionEntity representativeExecution = owningInstance.resolveRepresentativeExecution();
    representativeExecution.addExternalTask(externalTask);

    externalTask.setExecution(representativeExecution);
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  @Override
  public void migrateState() {
    ScopeImpl targetActivity = migratingActivityInstance.getTargetScope();
    ProcessDefinition targetProcessDefinition = (ProcessDefinition) targetActivity.getProcessDefinition();

    externalTask.setActivityId(targetActivity.getId());
    externalTask.setProcessDefinitionId(targetProcessDefinition.getId());
    externalTask.setProcessDefinitionKey(targetProcessDefinition.getKey());
  }

  public String getId() {
    return externalTask.getId();
  }

  public ScopeImpl getTargetScope() {
    return migratingActivityInstance.getTargetScope();
  }

  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
    dependentInstances.add(migratingInstance);
  }
}
