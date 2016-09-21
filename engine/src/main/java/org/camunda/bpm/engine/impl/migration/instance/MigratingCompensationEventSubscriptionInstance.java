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
public class MigratingCompensationEventSubscriptionInstance extends MigratingProcessElementInstance implements RemovingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected EventSubscriptionEntity eventSubscription;

  public MigratingCompensationEventSubscriptionInstance(
      MigrationInstruction migrationInstruction,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      EventSubscriptionEntity eventSubscription) {
    this.migrationInstruction = migrationInstruction;
    this.eventSubscription = eventSubscription;
    this.sourceScope = sourceScope;
    this.targetScope = targetScope;
    this.currentScope = sourceScope;
  }

  @Override
  public boolean isDetached() {
    return eventSubscription.getExecutionId() == null;
  }

  @Override
  public void detachState() {
    eventSubscription.setExecution(null);
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);

  }

  @Override
  public void migrateState() {
    eventSubscription.setActivity((ActivityImpl) targetScope);
    currentScope = targetScope;

  }

  @Override
  public void migrateDependentEntities() {
  }

  @Override
  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
  }

  @Override
  public ExecutionEntity resolveRepresentativeExecution() {
    return null;
  }

  @Override
  public void attachState(MigratingScopeInstance targetActivityInstance) {
    setParent(targetActivityInstance);

    ExecutionEntity representativeExecution = targetActivityInstance.resolveRepresentativeExecution();
    eventSubscription.setExecution(representativeExecution);
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

  public void remove() {
    eventSubscription.delete();
  }

}
