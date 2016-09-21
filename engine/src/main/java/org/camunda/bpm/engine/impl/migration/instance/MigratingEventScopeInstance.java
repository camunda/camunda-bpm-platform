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
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingEventScopeInstance extends MigratingScopeInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected MigratingCompensationEventSubscriptionInstance migratingEventSubscription;

  protected ExecutionEntity eventScopeExecution;

  protected Set<MigratingEventScopeInstance> childInstances = new HashSet<MigratingEventScopeInstance>();
  protected Set<MigratingCompensationEventSubscriptionInstance> childCompensationSubscriptionInstances = new HashSet<MigratingCompensationEventSubscriptionInstance>();
  protected List<MigratingInstance> migratingDependentInstances = new ArrayList<MigratingInstance>();

  public MigratingEventScopeInstance(
      MigrationInstruction migrationInstruction,
      ExecutionEntity eventScopeExecution,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      MigrationInstruction eventSubscriptionInstruction,
      EventSubscriptionEntity eventSubscription,
      ScopeImpl eventSubscriptionSourceScope,
      ScopeImpl eventSubscriptionTargetScope
      ) {
    this.migratingEventSubscription =
        new MigratingCompensationEventSubscriptionInstance(
            eventSubscriptionInstruction,
            eventSubscriptionSourceScope,
            eventSubscriptionTargetScope,
            eventSubscription);
    this.migrationInstruction = migrationInstruction;
    this.eventScopeExecution = eventScopeExecution;

    // compensation handlers (not boundary events)
    this.sourceScope = sourceScope;
    this.targetScope = targetScope;
  }

  /**
   * Creates an emerged scope
   */
  public MigratingEventScopeInstance(
      EventSubscriptionEntity eventSubscription,
      ExecutionEntity eventScopeExecution,
      ScopeImpl targetScope
      ) {
    this.migratingEventSubscription =
        new MigratingCompensationEventSubscriptionInstance(null, null, targetScope, eventSubscription);
    this.eventScopeExecution = eventScopeExecution;

    // compensation handlers (not boundary events)
    // or parent flow scopes
    this.targetScope = targetScope;
    this.currentScope = targetScope;
  }

  @Override
  public boolean isDetached() {
    return eventScopeExecution.getParentId() == null;
  }

  @Override
  public void detachState() {
    migratingEventSubscription.detachState();
    eventScopeExecution.setParent(null);
  }

  @Override
  public void attachState(MigratingScopeInstance targetActivityInstance) {
    setParent(targetActivityInstance);

    migratingEventSubscription.attachState(targetActivityInstance);

    ExecutionEntity representativeExecution = targetActivityInstance.resolveRepresentativeExecution();
    eventScopeExecution.setParent(representativeExecution);
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  @Override
  public void migrateState() {
    migratingEventSubscription.migrateState();

    eventScopeExecution.setActivity((ActivityImpl) targetScope);
    eventScopeExecution.setProcessDefinition(targetScope.getProcessDefinition());

    currentScope = targetScope;
  }

  @Override
  public void migrateDependentEntities() {
    for (MigratingInstance dependentEntity : migratingDependentInstances) {
      dependentEntity.migrateState();
    }
  }

  @Override
  public void setParent(MigratingScopeInstance parentInstance) {
    if (this.parentInstance != null) {
      this.parentInstance.removeChild(this);
    }

    this.parentInstance = parentInstance;

    if (parentInstance != null) {
      parentInstance.addChild(this);
    }
  }

  @Override
  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
    migratingDependentInstances.add(migratingInstance);
  }

  @Override
  public ExecutionEntity resolveRepresentativeExecution() {
    return eventScopeExecution;
  }

  @Override
  public void removeChild(MigratingScopeInstance migratingScopeInstance) {
    childInstances.remove(migratingScopeInstance);
  }

  @Override
  public void addChild(MigratingScopeInstance migratingScopeInstance) {
    if (migratingScopeInstance instanceof MigratingEventScopeInstance) {
      childInstances.add((MigratingEventScopeInstance) migratingScopeInstance);
    }
    else {
      throw MIGRATION_LOGGER.cannotHandleChild(this, migratingScopeInstance);
    }
  }

  @Override
  public void addChild(MigratingCompensationEventSubscriptionInstance migratingEventSubscription) {
    this.childCompensationSubscriptionInstances.add(migratingEventSubscription);
  }

  @Override
  public void removeChild(MigratingCompensationEventSubscriptionInstance migratingEventSubscription) {
    this.childCompensationSubscriptionInstances.remove(migratingEventSubscription);
  }

  @Override
  public boolean migrates() {
    return targetScope != null;
  }

  @Override
  public void detachChildren() {
    Set<MigratingProcessElementInstance> childrenCopy = new HashSet<MigratingProcessElementInstance>(getChildren());
    for (MigratingProcessElementInstance child : childrenCopy) {
      child.detachState();
    }
  }

  @Override
  public void remove(boolean skipCustomListeners, boolean skipIoMappings) {
    // never invokes listeners and io mappings because this does not remove an active
    // activity instance
    eventScopeExecution.remove();
    migratingEventSubscription.remove();
    setParent(null);
  }

  @Override
  public Collection<MigratingProcessElementInstance> getChildren() {
    Set<MigratingProcessElementInstance> children = new HashSet<MigratingProcessElementInstance>(childInstances);
    children.addAll(childCompensationSubscriptionInstances);
    return children;
  }

  @Override
  public Collection<MigratingScopeInstance> getChildScopeInstances() {
    return new HashSet<MigratingScopeInstance>(childInstances);
  }

  @Override
  public void removeUnmappedDependentInstances() {
  }

  public MigratingCompensationEventSubscriptionInstance getEventSubscription() {
    return migratingEventSubscription;
  }

}
