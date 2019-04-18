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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingProcessInstance {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected String processInstanceId;
  protected List<MigratingActivityInstance> migratingActivityInstances;
  protected List<MigratingTransitionInstance> migratingTransitionInstances;
  protected List<MigratingEventScopeInstance> migratingEventScopeInstances;
  protected List<MigratingCompensationEventSubscriptionInstance> migratingCompensationSubscriptionInstances;
  protected MigratingActivityInstance rootInstance;
  protected ProcessDefinitionEntity sourceDefinition;
  protected ProcessDefinitionEntity targetDefinition;

  public MigratingProcessInstance(String processInstanceId, ProcessDefinitionEntity sourceDefinition, ProcessDefinitionEntity targetDefinition) {
    this.processInstanceId = processInstanceId;
    this.migratingActivityInstances = new ArrayList<MigratingActivityInstance>();
    this.migratingTransitionInstances = new ArrayList<MigratingTransitionInstance>();
    this.migratingEventScopeInstances = new ArrayList<MigratingEventScopeInstance>();
    this.migratingCompensationSubscriptionInstances = new ArrayList<MigratingCompensationEventSubscriptionInstance>();
    this.sourceDefinition = sourceDefinition;
    this.targetDefinition = targetDefinition;
  }

  public MigratingActivityInstance getRootInstance() {
    return rootInstance;
  }

  public void setRootInstance(MigratingActivityInstance rootInstance) {
    this.rootInstance = rootInstance;
  }

  public Collection<MigratingActivityInstance> getMigratingActivityInstances() {
    return migratingActivityInstances;
  }

  public Collection<MigratingTransitionInstance> getMigratingTransitionInstances() {
    return migratingTransitionInstances;
  }

  public Collection<MigratingEventScopeInstance> getMigratingEventScopeInstances() {
    return migratingEventScopeInstances;
  }

  public Collection<MigratingCompensationEventSubscriptionInstance> getMigratingCompensationSubscriptionInstances() {
    return migratingCompensationSubscriptionInstances;
  }

  public Collection<MigratingScopeInstance> getMigratingScopeInstances() {
    Set<MigratingScopeInstance> result = new HashSet<MigratingScopeInstance>();

    result.addAll(migratingActivityInstances);
    result.addAll(migratingEventScopeInstances);

    return result;
  }

  public ProcessDefinitionEntity getSourceDefinition() {
    return sourceDefinition;
  }

  public ProcessDefinitionEntity getTargetDefinition() {
    return targetDefinition;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public MigratingActivityInstance addActivityInstance(
      MigrationInstruction migrationInstruction,
      ActivityInstance activityInstance,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity scopeExecution) {

    MigratingActivityInstance migratingActivityInstance = new MigratingActivityInstance(
        activityInstance,
        migrationInstruction,
        sourceScope,
        targetScope,
        scopeExecution);

    migratingActivityInstances.add(migratingActivityInstance);

    if (processInstanceId.equals(activityInstance.getId())) {
      rootInstance = migratingActivityInstance;
    }

    return migratingActivityInstance;
  }

  public MigratingTransitionInstance addTransitionInstance(
      MigrationInstruction migrationInstruction,
      TransitionInstance transitionInstance,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity asyncExecution) {

    MigratingTransitionInstance migratingTransitionInstance = new MigratingTransitionInstance(
        transitionInstance,
        migrationInstruction,
        sourceScope,
        targetScope,
        asyncExecution);

    migratingTransitionInstances.add(migratingTransitionInstance);

    return migratingTransitionInstance;
  }

  public MigratingEventScopeInstance addEventScopeInstance(
      MigrationInstruction migrationInstruction,
      ExecutionEntity eventScopeExecution,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      MigrationInstruction eventSubscriptionInstruction,
      EventSubscriptionEntity eventSubscription,
      ScopeImpl eventSubscriptionSourceScope,
      ScopeImpl eventSubscriptionTargetScope) {

    MigratingEventScopeInstance compensationInstance = new MigratingEventScopeInstance(
        migrationInstruction,
        eventScopeExecution,
        sourceScope,
        targetScope,
        eventSubscriptionInstruction,
        eventSubscription,
        eventSubscriptionSourceScope,
        eventSubscriptionTargetScope);

    migratingEventScopeInstances.add(compensationInstance);

    return compensationInstance;
  }

  public MigratingCompensationEventSubscriptionInstance addCompensationSubscriptionInstance(
      MigrationInstruction eventSubscriptionInstruction,
      EventSubscriptionEntity eventSubscription,
      ScopeImpl sourceScope,
      ScopeImpl targetScope) {
    MigratingCompensationEventSubscriptionInstance compensationInstance = new MigratingCompensationEventSubscriptionInstance(
        eventSubscriptionInstruction,
        sourceScope,
        targetScope,
        eventSubscription);

    migratingCompensationSubscriptionInstances.add(compensationInstance);

    return compensationInstance;
  }

}
