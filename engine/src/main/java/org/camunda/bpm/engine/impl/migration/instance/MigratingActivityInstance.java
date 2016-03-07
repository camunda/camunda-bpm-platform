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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingActivityInstance implements MigratingInstance, RemovingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected MigrationInstruction migrationInstruction;
  protected ActivityInstance activityInstance;
  // scope execution for actual scopes,
  // concurrent execution in case of non-scope activity with expanded tree
  protected ExecutionEntity representativeExecution;

  protected List<RemovingInstance> removingDependentInstances = new ArrayList<RemovingInstance>();
  protected List<MigratingInstance> migratingDependentInstances = new ArrayList<MigratingInstance>();
  protected List<EmergingInstance> emergingDependentInstances = new ArrayList<EmergingInstance>();

  protected ScopeImpl sourceScope;
  protected ScopeImpl targetScope;
  // changes from source to target scope during migration
  protected ScopeImpl currentScope;

  protected Set<MigratingActivityInstance> childInstances = new HashSet<MigratingActivityInstance>();
  protected MigratingActivityInstance parentInstance;

  // behaves differently if the current activity is scope or not
  protected MigratingActivityInstanceBehavior instanceBehavior;

  /**
   * Creates a migrating activity instances
   */
  public MigratingActivityInstance(ActivityInstance activityInstance,
      MigrationInstruction migrationInstruction,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity scopeExecution) {

    this.activityInstance = activityInstance;
    this.migrationInstruction = migrationInstruction;
    this.sourceScope = sourceScope;
    this.currentScope = sourceScope;
    this.targetScope = targetScope;
    this.representativeExecution = scopeExecution;
    this.instanceBehavior = determineBehavior(sourceScope);
  }

  /**
   * Creates an emerged activity instance
   */
  public MigratingActivityInstance(ScopeImpl targetScope, ExecutionEntity scopeExecution) {

    this.targetScope = targetScope;
    this.currentScope = targetScope;
    this.representativeExecution = scopeExecution;
    this.instanceBehavior = determineBehavior(targetScope);
  }


  protected MigratingActivityInstanceBehavior determineBehavior(ScopeImpl scope) {
    if (scope.isScope()) {
      return new MigratingScopeActivityInstanceBehavior();
    }
    else {
      return new MigratingNonScopeActivityInstanceBehavior();
    }
  }

  public void detachState() {

    instanceBehavior.detachState();

    if (parentInstance != null) {
      parentInstance.getChildren().remove(this);
      parentInstance = null;
    }
  }

  public void attachState(MigratingActivityInstance activityInstance) {

    activityInstance.getChildren().add(this);
    this.setParent(activityInstance);
    instanceBehavior.attachState(activityInstance);
  }

  public void migrateDependentEntities() {
    for (MigratingInstance migratingInstance : migratingDependentInstances) {
      migratingInstance.migrateState();
      migratingInstance.migrateDependentEntities();
    }

    ExecutionEntity representativeExecution = resolveRepresentativeExecution();
    for (EmergingInstance emergingInstance : emergingDependentInstances) {
      emergingInstance.create(representativeExecution);
    }
  }

  public ExecutionEntity resolveRepresentativeExecution() {
    return instanceBehavior.resolveRepresentativeExecution();
  }

  public void addMigratingDependentInstance(MigratingInstance migratingInstance) {
    migratingDependentInstances.add(migratingInstance);
  }

  public void addRemovingDependentInstance(RemovingInstance removingInstance) {
    removingDependentInstances.add(removingInstance);
  }

  public void addEmergingDependentInstance(EmergingInstance emergingInstance) {
    emergingDependentInstances.add(emergingInstance);
  }

  public ActivityInstance getActivityInstance() {
    return activityInstance;
  }

  public ScopeImpl getSourceScope() {
    return sourceScope;
  }

  public ScopeImpl getTargetScope() {
    return targetScope;
  }

  public Set<MigratingActivityInstance> getChildren() {
    return childInstances;
  }

  public MigratingActivityInstance getParent() {
    return parentInstance;
  }

  public void setParent(MigratingActivityInstance parentInstance) {
    this.parentInstance = parentInstance;
  }

  public MigrationInstruction getMigrationInstruction() {
    return migrationInstruction;
  }

  public boolean migrates() {
    return targetScope != null;
  }

  public void removeUnmappedDependentInstances() {
    for (RemovingInstance removingInstance : removingDependentInstances) {
      removingInstance.remove();
    }
  }

  @Override
  public void remove() {
    instanceBehavior.remove();
  }

  @Override
  public void migrateState() {
    instanceBehavior.migrateState();
    currentScope = targetScope;
  }

  public ExecutionEntity createAttachableExecution() {
    return instanceBehavior.createAttachableExecution();
  }

  public void destroyAttachableExecution(ExecutionEntity execution) {
    instanceBehavior.destroyAttachableExecution(execution);
  }



  protected interface MigratingActivityInstanceBehavior {

    void detachState();

    void attachState(MigratingActivityInstance parentInstance);

    void migrateState();

    void remove();

    ExecutionEntity resolveRepresentativeExecution();

    ExecutionEntity createAttachableExecution();

    void destroyAttachableExecution(ExecutionEntity execution);
  }

  protected class MigratingNonScopeActivityInstanceBehavior implements MigratingActivityInstanceBehavior {

    @Override
    public void detachState() {
      ExecutionEntity currentExecution = resolveRepresentativeExecution();

      currentExecution.setActivity(null);
      currentExecution.leaveActivityInstance();

      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.detachState();
      }

      parentInstance.destroyAttachableExecution(currentExecution);

    }

    @Override
    public void attachState(MigratingActivityInstance newParentInstance) {

      representativeExecution = newParentInstance.createAttachableExecution();

      representativeExecution.setActivity((PvmActivity) sourceScope);
      representativeExecution.setActivityInstanceId(activityInstance.getId());

      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.attachState(MigratingActivityInstance.this);
      }

    }

    @Override
    public void migrateState() {
      ExecutionEntity currentExecution = resolveRepresentativeExecution();
      currentExecution.setProcessDefinition(targetScope.getProcessDefinition());
      currentExecution.setActivity((PvmActivity) targetScope);

      if (targetScope.isScope()) {
        becomeScope();
      }
    }

    protected void becomeScope() {
      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.detachState();
      }

      ExecutionEntity currentExecution = resolveRepresentativeExecution();

      currentExecution = currentExecution.createExecution();
      ExecutionEntity parent = currentExecution.getParent();
      parent.setActivity(null);

      if (!parent.isConcurrent()) {
        parent.leaveActivityInstance();
      }

      representativeExecution = currentExecution;
      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.attachState(MigratingActivityInstance.this);
      }

      instanceBehavior = new MigratingScopeActivityInstanceBehavior();
    }

    @Override
    public ExecutionEntity resolveRepresentativeExecution() {
      if (representativeExecution.getReplacedBy() != null) {
        return representativeExecution.resolveReplacedBy();
      }
      else {
        return representativeExecution;
      }
    }

    @Override
    public void remove() {
      // nothing to do; we don't remove non-scope instances
    }

    @Override
    public ExecutionEntity createAttachableExecution() {
      throw MIGRATION_LOGGER.cannotBecomeSubordinateInNonScope(MigratingActivityInstance.this);
    }

    @Override
    public void destroyAttachableExecution(ExecutionEntity execution) {
      throw MIGRATION_LOGGER.cannotDestroySubordinateInNonScope(MigratingActivityInstance.this);
    }
  }

  protected class MigratingScopeActivityInstanceBehavior implements MigratingActivityInstanceBehavior {

    @Override
    public void detachState() {
      ExecutionEntity currentScopeExecution = resolveRepresentativeExecution();

      ExecutionEntity parentExecution = currentScopeExecution.getParent();
      currentScopeExecution.setParent(null);

      if (sourceScope.getActivityBehavior() instanceof CompositeActivityBehavior) {
        parentExecution.leaveActivityInstance();
      }
      parentInstance.destroyAttachableExecution(parentExecution);
    }

    @Override
    public void attachState(MigratingActivityInstance parentInstance) {
      ExecutionEntity newParentExecution = parentInstance.createAttachableExecution();

      ExecutionEntity currentScopeExecution = resolveRepresentativeExecution();
      currentScopeExecution.setParent(newParentExecution);

      if (sourceScope.getActivityBehavior() instanceof CompositeActivityBehavior) {
        newParentExecution.setActivityInstanceId(activityInstance.getId());
      }

    }

    @Override
    public void migrateState() {
      ExecutionEntity currentScopeExecution = resolveRepresentativeExecution();
      currentScopeExecution.setProcessDefinition(targetScope.getProcessDefinition());

      ExecutionEntity parentExecution = currentScopeExecution.getParent();

      if (parentExecution != null && parentExecution.isConcurrent()) {
        parentExecution.setProcessDefinition(targetScope.getProcessDefinition());
      }

      if (!targetScope.isScope()) {
        becomeNonScope();
        currentScopeExecution = resolveRepresentativeExecution();
      }

      if (isLeafActivity(targetScope)) {
        currentScopeExecution.setActivity((PvmActivity) targetScope);
      }

      if (sourceScope.getActivityBehavior() instanceof MigrationObserverBehavior) {
        ((MigrationObserverBehavior) sourceScope.getActivityBehavior()).migrateScope(currentScopeExecution);
      }
    }

    protected void becomeNonScope() {
      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.detachState();
      }

      ExecutionEntity parentExecution = representativeExecution.getParent();

      parentExecution.setActivity(representativeExecution.getActivity());
      parentExecution.setActivityInstanceId(representativeExecution.getActivityInstanceId());

      representativeExecution.remove();
      representativeExecution = parentExecution;

      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.attachState(MigratingActivityInstance.this);
      }

      instanceBehavior = new MigratingNonScopeActivityInstanceBehavior();
    }

    protected boolean isLeafActivity(ScopeImpl scope) {
      return scope.getActivities().isEmpty();
    }

    @Override
    public ExecutionEntity resolveRepresentativeExecution() {
      return representativeExecution;
    }

    @Override
    public void remove() {
      parentInstance.getChildren().remove(MigratingActivityInstance.this);
      for (MigratingActivityInstance child : childInstances) {
        child.parentInstance = null;
      }

      ExecutionEntity currentExecution = resolveRepresentativeExecution();
      ExecutionEntity parentExecution = currentExecution.getParent();

      currentExecution.setActivity((PvmActivity) sourceScope);
      currentExecution.setActivityInstanceId(activityInstance.getId());

      currentExecution.deleteCascade("migration");

      parentInstance.destroyAttachableExecution(parentExecution);
    }

    @Override
    public ExecutionEntity createAttachableExecution() {
      ExecutionEntity scopeExecution = resolveRepresentativeExecution();
      ExecutionEntity attachableExecution = scopeExecution;

      if (currentScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
        ModificationObserverBehavior behavior = (ModificationObserverBehavior) currentScope.getActivityBehavior();
        attachableExecution = (ExecutionEntity) behavior.createInnerInstance(scopeExecution);
      }
      else {
        if (!scopeExecution.getNonEventScopeExecutions().isEmpty() || scopeExecution.getActivity() != null) {
          attachableExecution = (ExecutionEntity) scopeExecution.createConcurrentExecution();
          scopeExecution.forceUpdate();
        }
      }

      return attachableExecution;
    }

    @Override
    public void destroyAttachableExecution(ExecutionEntity execution) {

      if (currentScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
        ModificationObserverBehavior behavior = (ModificationObserverBehavior) currentScope.getActivityBehavior();
        behavior.destroyInnerInstance(execution);

      }
      else {
        if (execution.isConcurrent()) {
          execution.remove();
          execution.getParent().tryPruneLastConcurrentChild();
          execution.getParent().forceUpdate();
        }
      }
    }
  }


}


