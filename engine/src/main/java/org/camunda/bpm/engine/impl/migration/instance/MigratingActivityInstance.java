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
public class MigratingActivityInstance extends MigratingProcessElementInstance implements MigratingInstance, RemovingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected ActivityInstance activityInstance;
  // scope execution for actual scopes,
  // concurrent execution in case of non-scope activity with expanded tree
  protected ExecutionEntity representativeExecution;

  protected List<RemovingInstance> removingDependentInstances = new ArrayList<RemovingInstance>();
  protected List<MigratingInstance> migratingDependentInstances = new ArrayList<MigratingInstance>();
  protected List<EmergingInstance> emergingDependentInstances = new ArrayList<EmergingInstance>();

  protected Set<MigratingActivityInstance> childActivityInstances = new HashSet<MigratingActivityInstance>();
  protected Set<MigratingTransitionInstance> childTransitionInstances = new HashSet<MigratingTransitionInstance>();

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

  public void detachChildren() {
    Set<MigratingActivityInstance> childrenCopy = new HashSet<MigratingActivityInstance>(childActivityInstances);
    // First detach all dependent entities, only then detach the activity instances.
    // This is because detaching activity instances may trigger execution tree compaction which in turn
    // may overwrite certain dependent entities (e.g. variables)
    for (MigratingActivityInstance child : childrenCopy) {
      child.detachDependentInstances();
    }

    for (MigratingActivityInstance child : childrenCopy) {
      child.detachState();
    }

    Set<MigratingTransitionInstance> transitionChildrenCopy = new HashSet<MigratingTransitionInstance>(childTransitionInstances);
    for (MigratingTransitionInstance child : transitionChildrenCopy) {
      child.detachState();
    }
  }

  public void detachDependentInstances() {
    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      if (!dependentInstance.isDetached()) {
        dependentInstance.detachState();
      }
    }
  }

  @Override
  public boolean isDetached() {
    return instanceBehavior.isDetached();
  }

  public void detachState() {

    detachDependentInstances();

    instanceBehavior.detachState();

    setParent(null);
  }

  public void attachState(MigratingActivityInstance activityInstance) {

    this.setParent(activityInstance);
    instanceBehavior.attachState(activityInstance);

    for (MigratingInstance dependentInstance : migratingDependentInstances) {
      dependentInstance.attachState(this);
    }
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTranisitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
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

  public List<MigratingInstance> getMigratingDependentInstances() {
    return migratingDependentInstances;
  }

  public void addRemovingDependentInstance(RemovingInstance removingInstance) {
    removingDependentInstances.add(removingInstance);
  }

  public void addEmergingDependentInstance(EmergingInstance emergingInstance) {
    emergingDependentInstances.add(emergingInstance);
  }

  public void addChild(MigratingTransitionInstance transitionInstance) {
    this.childTransitionInstances.add(transitionInstance);
  }

  public void removeChild(MigratingTransitionInstance transitionInstance) {
    this.childTransitionInstances.remove(transitionInstance);
  }

  public void addChild(MigratingActivityInstance activityInstance) {
    this.childActivityInstances.add(activityInstance);
  }

  public void removeChild(MigratingActivityInstance activityInstance) {
    this.childActivityInstances.remove(activityInstance);
  }

  public ActivityInstance getActivityInstance() {
    return activityInstance;
  }

  /**
   * Returns a copy of all children, modifying the returned set does not have any further effect.
   * @return
   */
  public Set<MigratingProcessElementInstance> getChildren() {
    Set<MigratingProcessElementInstance> childInstances = new HashSet<MigratingProcessElementInstance>();
    childInstances.addAll(childActivityInstances);
    childInstances.addAll(childTransitionInstances);
    return childInstances;
  }

  public Set<MigratingActivityInstance> getChildActivityInstances() {
    return childActivityInstances;
  }

  public Set<MigratingTransitionInstance> getChildTransitionInstances() {
    return childTransitionInstances;
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
  }

  public ExecutionEntity createAttachableExecution() {
    return instanceBehavior.createAttachableExecution();
  }

  public void destroyAttachableExecution(ExecutionEntity execution) {
    instanceBehavior.destroyAttachableExecution(execution);
  }

  @Override
  public void setParent(MigratingActivityInstance parentInstance) {
    if (this.parentInstance != null) {
      this.parentInstance.removeChild(this);
    }

    this.parentInstance = parentInstance;

    if (parentInstance != null) {
      parentInstance.addChild(this);
    }
  }


  protected interface MigratingActivityInstanceBehavior {

    boolean isDetached();

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
    public boolean isDetached() {
      return resolveRepresentativeExecution().getActivity() == null;
    }

    @Override
    public void detachState() {
      ExecutionEntity currentExecution = resolveRepresentativeExecution();

      currentExecution.setActivity(null);
      currentExecution.leaveActivityInstance();

      parentInstance.destroyAttachableExecution(currentExecution);
    }

    @Override
    public void attachState(MigratingActivityInstance newParentInstance) {

      representativeExecution = newParentInstance.createAttachableExecution();

      representativeExecution.setActivity((PvmActivity) sourceScope);
      representativeExecution.setActivityInstanceId(activityInstance.getId());

    }

    @Override
    public void migrateState() {
      ExecutionEntity currentExecution = resolveRepresentativeExecution();
      currentExecution.setProcessDefinition(targetScope.getProcessDefinition());
      currentExecution.setActivity((PvmActivity) targetScope);

      currentScope = targetScope;

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
    public boolean isDetached() {
      ExecutionEntity representativeExecution = resolveRepresentativeExecution();
      return representativeExecution != representativeExecution.getProcessInstance()
        && representativeExecution.getParent() == null;
    }

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

      currentScope = targetScope;

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

      ExecutionEntity currentExecution = resolveRepresentativeExecution();
      ExecutionEntity parentExecution = currentExecution.getParent();

      currentExecution.setActivity((PvmActivity) sourceScope);
      currentExecution.setActivityInstanceId(activityInstance.getId());

      currentExecution.deleteCascade("migration");

      parentInstance.destroyAttachableExecution(parentExecution);

      setParent(null);
      for (MigratingTransitionInstance child : childTransitionInstances) {
        child.setParent(null);
      }
      for (MigratingActivityInstance child : childActivityInstances) {
        child.setParent(null);
      }
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


