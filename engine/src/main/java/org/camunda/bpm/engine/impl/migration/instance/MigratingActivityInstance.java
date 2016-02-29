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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingActivityInstance implements MigratingInstance, RemovingInstance {

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

  protected Set<MigratingActivityInstance> childInstances = new HashSet<MigratingActivityInstance>();
  protected MigratingActivityInstance parentInstance;

  // behaves differently if the current activity is scope or not
  protected MigratingActivityInstanceBehavior instanceBehavior;

  public MigratingActivityInstance(ActivityInstance activityInstance,
      MigrationInstruction migrationInstruction,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity scopeExecution) {
    this.activityInstance = activityInstance;
    this.migrationInstruction = migrationInstruction;
    this.sourceScope = sourceScope;
    this.targetScope = targetScope;
    this.representativeExecution = scopeExecution;

    if (sourceScope.isScope()) {
      this.instanceBehavior = new MigratingScopeActivityInstanceBehavior();
    }
    else {
      this.instanceBehavior = new MigratingNonScopeActivityInstanceBehavior();
    }
  }

  public void detachState() {
    instanceBehavior.detachState();
  }

  public void attachState(ExecutionEntity newScopeExecution) {
    instanceBehavior.attachState(newScopeExecution);
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
  }

  protected interface MigratingActivityInstanceBehavior {

    void detachState();

    void attachState(ExecutionEntity representativeExecution);

    void migrateState();

    void remove();

    ExecutionEntity resolveRepresentativeExecution();
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

      if (!currentExecution.isScope()) {
        ExecutionEntity parent = currentExecution.getParent();
        currentExecution.remove();
        parent.tryPruneLastConcurrentChild();
        parent.forceUpdate();
      }

    }

    @Override
    public void attachState(ExecutionEntity newScopeExecution) {

      representativeExecution = newScopeExecution;
      if (!newScopeExecution.getNonEventScopeExecutions().isEmpty() || newScopeExecution.getActivity() != null) {
        representativeExecution = (ExecutionEntity) newScopeExecution.createConcurrentExecution();
        newScopeExecution.forceUpdate();
      }

      representativeExecution.setActivity((PvmActivity) sourceScope);
      representativeExecution.setActivityInstanceId(activityInstance.getId());

      for (MigratingInstance dependentInstance : migratingDependentInstances) {
        dependentInstance.attachState(representativeExecution);
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
        dependentInstance.attachState(currentExecution);
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
  }

  protected class MigratingScopeActivityInstanceBehavior implements MigratingActivityInstanceBehavior {

    @Override
    public void detachState() {
      ExecutionEntity currentScopeExecution = resolveRepresentativeExecution();

      ExecutionEntity parentExecution = currentScopeExecution.getParent();
      ExecutionEntity parentScopeExecution = parentExecution.isConcurrent() ? parentExecution.getParent() : parentExecution;
      currentScopeExecution.setParent(null);

      if (parentExecution.isConcurrent()) {
        parentExecution.remove();
        parentScopeExecution.tryPruneLastConcurrentChild();
        parentScopeExecution.forceUpdate();
      }
      else {
        if (sourceScope.getActivityBehavior() instanceof CompositeActivityBehavior) {
          parentExecution.leaveActivityInstance();
        }
      }

    }

    @Override
    public void attachState(ExecutionEntity newScopeExecution) {
      ExecutionEntity newParentExecution = newScopeExecution;
      if (!newScopeExecution.getNonEventScopeExecutions().isEmpty()) {
        newParentExecution = (ExecutionEntity) newScopeExecution.createConcurrentExecution();
        newScopeExecution.forceUpdate();
      }

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
        dependentInstance.attachState(representativeExecution);
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

      if (parentExecution.isConcurrent()) {
        ExecutionEntity grandParent = parentExecution.getParent();
        parentExecution.remove();
        grandParent.tryPruneLastConcurrentChild();
        grandParent.forceUpdate();
      }

    }
  }


}


