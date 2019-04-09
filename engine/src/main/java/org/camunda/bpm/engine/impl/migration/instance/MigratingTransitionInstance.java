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
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingTransitionInstance extends MigratingProcessElementInstance implements MigratingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected ExecutionEntity representativeExecution;

  protected TransitionInstance transitionInstance;
  protected MigratingAsyncJobInstance jobInstance;
  protected List<MigratingInstance> migratingDependentInstances = new ArrayList<MigratingInstance>();
  protected boolean activeState;


  public MigratingTransitionInstance(
      TransitionInstance transitionInstance,
      MigrationInstruction migrationInstruction,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity asyncExecution) {
    this.transitionInstance = transitionInstance;
    this.migrationInstruction = migrationInstruction;
    this.sourceScope = sourceScope;
    this.targetScope = targetScope;
    this.currentScope = sourceScope;
    this.representativeExecution = asyncExecution;
    this.activeState = representativeExecution.isActive();
  }

  @Override
  public boolean isDetached() {
    return jobInstance.isDetached();
  }

  @Override
  public MigratingActivityInstance getParent() {
    return (MigratingActivityInstance) super.getParent();
  }

  @Override
  public void detachState() {

    jobInstance.detachState();
    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.detachState();
    }

    ExecutionEntity execution = resolveRepresentativeExecution();
    execution.setActive(false);
    getParent().destroyAttachableExecution(execution);

    setParent(null);
  }

  @Override
  public void attachState(MigratingScopeInstance scopeInstance) {
    if (!(scopeInstance instanceof MigratingActivityInstance)) {
      throw MIGRATION_LOGGER.cannotHandleChild(scopeInstance, this);
    }

    MigratingActivityInstance activityInstance = (MigratingActivityInstance) scopeInstance;

    setParent(activityInstance);

    representativeExecution = activityInstance.createAttachableExecution();
    representativeExecution.setActivityInstanceId(null);
    representativeExecution.setActive(activeState);

    jobInstance.attachState(this);

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.attachState(this);
    }
  }

  public ExecutionEntity resolveRepresentativeExecution() {
    if (representativeExecution.getReplacedBy() != null) {
      return representativeExecution.resolveReplacedBy();
    }
    else {
      return representativeExecution;
    }
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {

    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  public void setDependentJobInstance(MigratingAsyncJobInstance jobInstance) {
    this.jobInstance = jobInstance;
  }

  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
    migratingDependentInstances.add(migratingInstance);
  }

  public List<MigratingInstance> getMigratingDependentInstances() {
    return migratingDependentInstances;
  }

  @Override
  public void migrateState() {
    ExecutionEntity representativeExecution = resolveRepresentativeExecution();

    representativeExecution.setProcessDefinition(targetScope.getProcessDefinition());
    representativeExecution.setActivity((PvmActivity) targetScope);
  }

  @Override
  public void migrateDependentEntities() {
    jobInstance.migrateState();
    jobInstance.migrateDependentEntities();

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.migrateState();
      dependentInstance.migrateDependentEntities();
    }
  }

  public TransitionInstance getTransitionInstance() {
    return transitionInstance;
  }

  /**
   * Else asyncBefore
   */
  public boolean isAsyncAfter() {
    return jobInstance.isAsyncAfter();
  }

  public boolean isAsyncBefore() {
    return jobInstance.isAsyncBefore();
  }

  public MigratingJobInstance getJobInstance() {
    return jobInstance;
  }

  @Override
  public void setParent(MigratingScopeInstance parentInstance) {
    if (parentInstance != null && !(parentInstance instanceof MigratingActivityInstance)) {
      throw MIGRATION_LOGGER.cannotHandleChild(parentInstance, this);
    }

    MigratingActivityInstance parentActivityInstance = (MigratingActivityInstance) parentInstance;

    if (this.parentInstance != null) {
      ((MigratingActivityInstance) this.parentInstance).removeChild(this);
    }

    this.parentInstance = parentActivityInstance;

    if (parentInstance != null) {
      parentActivityInstance.addChild(this);
    }
  }

}
