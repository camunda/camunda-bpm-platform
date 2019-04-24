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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public abstract class MigratingJobInstance implements MigratingInstance, RemovingInstance {

  protected JobEntity jobEntity;
  protected JobDefinitionEntity targetJobDefinitionEntity;
  protected ScopeImpl targetScope;

  protected List<MigratingInstance> migratingDependentInstances = new ArrayList<MigratingInstance>();

  public MigratingJobInstance(JobEntity jobEntity, JobDefinitionEntity jobDefinitionEntity,
      ScopeImpl targetScope) {
    this.jobEntity = jobEntity;
    this.targetJobDefinitionEntity = jobDefinitionEntity;
    this.targetScope = targetScope;
  }

  public MigratingJobInstance(JobEntity jobEntity) {
    this(jobEntity, null, null);
  }

  public JobEntity getJobEntity() {
    return jobEntity;
  }

  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
    migratingDependentInstances.add(migratingInstance);
  }

  @Override
  public boolean isDetached() {
    return jobEntity.getExecutionId() == null;
  }

  public void detachState() {
    jobEntity.setExecution(null);

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.detachState();
    }
  }

  public void attachState(MigratingScopeInstance newOwningInstance) {
    attachTo(newOwningInstance.resolveRepresentativeExecution());

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.attachState(newOwningInstance);
    }
  }

  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    attachTo(targetTransitionInstance.resolveRepresentativeExecution());

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.attachState(targetTransitionInstance);
    }
  }

  protected void attachTo(ExecutionEntity execution) {
    jobEntity.setExecution(execution);
  }

  public void migrateState() {
    // update activity reference
    String activityId = targetScope.getId();
    jobEntity.setActivityId(activityId);
    migrateJobHandlerConfiguration();

    if (targetJobDefinitionEntity != null) {
      jobEntity.setJobDefinition(targetJobDefinitionEntity);
    }

    // update process definition reference
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) targetScope.getProcessDefinition();
    jobEntity.setProcessDefinitionId(processDefinition.getId());
    jobEntity.setProcessDefinitionKey(processDefinition.getKey());

    // update deployment reference
    jobEntity.setDeploymentId(processDefinition.getDeploymentId());
  }

  public void migrateDependentEntities() {
    for (MigratingInstance migratingDependentInstance : migratingDependentInstances) {
      migratingDependentInstance.migrateState();
    }
  }

  public void remove() {
    jobEntity.delete();
  }

  public boolean migrates() {
    return targetScope != null;
  }

  public ScopeImpl getTargetScope() {
    return targetScope;
  }

  public JobDefinitionEntity getTargetJobDefinitionEntity() {
    return targetJobDefinitionEntity;
  }

  protected abstract void migrateJobHandlerConfiguration();

}
