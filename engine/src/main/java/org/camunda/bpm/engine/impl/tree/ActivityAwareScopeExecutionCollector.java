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
package org.camunda.bpm.engine.impl.tree;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * <p>Only collects scope executions that execute ancestor scopes of the given commonChildScope.</p>
 *
 * <p>In regular process execution, collects all scope executions. Yet in cases where
 * the execution tree is not aligned to the activity tree (such as with compensation throwing executions),
 * executions are skipped if they execute an activity that is not a parent/ancestor of the
 * given commonChildScope.</p>
 *
 * @author Thorben Lindhauer
 *
 */
public class ActivityAwareScopeExecutionCollector implements TreeVisitor<PvmExecutionImpl> {

  protected ScopeImpl commonChildScope;
  protected ScopeImpl currentActivity;
  protected List<PvmExecutionImpl> executions = new ArrayList<PvmExecutionImpl>();

  public ActivityAwareScopeExecutionCollector(ScopeImpl commonChildScope) {
    this.commonChildScope = commonChildScope;
    this.currentActivity = commonChildScope;
  }

  public void visit(PvmExecutionImpl obj) {
    if (obj.getActivity() != null) {
      // if an execution has an activity set, take this as a hint to
      // where we are in the activity tree and reset the currentActivity
      currentActivity = obj.getActivity();
    }

    if(obj.isScope()) {
      // 1: scope execution not executing a scope activity or
      // 2: scope execution is async before scope activity
      //    (in which case the dedicated execution for that scope does not yet exist)
      if (!currentActivity.isScope() || obj.getActivityInstanceId() == null) {
        currentActivity = getFlowScope(currentActivity);
      }

      if (isAncestorOf(currentActivity, commonChildScope)) {
        executions.add(obj);
      }

      currentActivity = getFlowScope(currentActivity);
    }

  }

  public List<PvmExecutionImpl> getExecutions() {
    return executions;
  }

  protected boolean isAncestorOf(ScopeImpl ancestorScope, ScopeImpl candidateDescendantScope) {
    if (candidateDescendantScope == ancestorScope) {
      return true;
    }
    else if (candidateDescendantScope.getFlowScope() != null) {
      return isAncestorOf(ancestorScope, candidateDescendantScope.getFlowScope());
    }
    else {
      return false;
    }
  }

  protected ScopeImpl getFlowScope(ScopeImpl scope) {
    if (scope != scope.getProcessDefinition()) {
      return scope.getFlowScope();
    }
    else {
      return scope;
    }
  }

}
