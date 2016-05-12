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

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstanceTopDownWalker.MigrationContext;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class MigratingProcessElementInstanceVisitor implements TreeVisitor<MigrationContext> {

  @Override
  public void visit(MigrationContext obj) {
    if (canMigrate(obj.processElementInstance)) {
      migrateProcessElementInstance(obj.processElementInstance, obj.scopeInstanceBranch);
    }
  }

  protected abstract boolean canMigrate(MigratingProcessElementInstance instance);

  protected abstract void instantiateScopes(
      MigratingScopeInstance ancestorScopeInstance,
      MigratingScopeInstanceBranch executionBranch,
      List<ScopeImpl> scopesToInstantiate);

  protected void migrateProcessElementInstance(MigratingProcessElementInstance migratingInstance, MigratingScopeInstanceBranch migratingInstanceBranch) {
    final MigratingScopeInstance parentMigratingInstance = migratingInstance.getParent();

    ScopeImpl sourceScope = migratingInstance.getSourceScope();
    ScopeImpl targetScope = migratingInstance.getTargetScope();
    ScopeImpl targetFlowScope = targetScope.getFlowScope();
    ScopeImpl parentActivityInstanceTargetScope = parentMigratingInstance != null ? parentMigratingInstance.getTargetScope() : null;

    if (sourceScope != sourceScope.getProcessDefinition() && targetFlowScope != parentActivityInstanceTargetScope) {
      // create intermediate scopes

      // 1. manipulate execution tree

      // determine the list of ancestor scopes (parent, grandparent, etc.) for which
      //     no executions exist yet
      List<ScopeImpl> nonExistingScopes = collectNonExistingFlowScopes(targetFlowScope, migratingInstanceBranch);

      // get the closest ancestor scope that is instantiated already
      ScopeImpl existingScope = nonExistingScopes.isEmpty() ?
          targetFlowScope :
          nonExistingScopes.get(0).getFlowScope();

      // and its scope instance
      MigratingScopeInstance ancestorScopeInstance = migratingInstanceBranch.getInstance(existingScope);

      // Instantiate the scopes as children of the scope execution
      instantiateScopes(ancestorScopeInstance, migratingInstanceBranch, nonExistingScopes);

      MigratingScopeInstance targetFlowScopeInstance = migratingInstanceBranch.getInstance(targetFlowScope);

      // 2. detach instance
      // The order of steps 1 and 2 avoids intermediate execution tree compaction
      // which in turn could overwrite some dependent instances (e.g. variables)
      migratingInstance.detachState();

      // 3. attach to newly created activity instance
      migratingInstance.attachState(targetFlowScopeInstance);
    }

    // 4. update state (e.g. activity id)
    migratingInstance.migrateState();

    // 5. migrate instance state other than execution-tree structure
    migratingInstance.migrateDependentEntities();
  }

  /**
   * Returns a list of flow scopes from the given scope until a scope is reached that is already present in the given
   * {@link MigratingScopeInstanceBranch} (exclusive). The order of the returned list is top-down, i.e. the highest scope
   * is the first element of the list.
   */
  protected List<ScopeImpl> collectNonExistingFlowScopes(ScopeImpl scope, final MigratingScopeInstanceBranch migratingExecutionBranch) {
    FlowScopeWalker walker = new FlowScopeWalker(scope);
    final List<ScopeImpl> result = new LinkedList<ScopeImpl>();
    walker.addPreVisitor(new TreeVisitor<ScopeImpl>() {

      @Override
      public void visit(ScopeImpl obj) {
        result.add(0, obj);
      }
    });

    walker.walkWhile(new ReferenceWalker.WalkCondition<ScopeImpl>() {

      @Override
      public boolean isFulfilled(ScopeImpl element) {
        return migratingExecutionBranch.hasInstance(element);
      }
    });

    return result;
  }

}
