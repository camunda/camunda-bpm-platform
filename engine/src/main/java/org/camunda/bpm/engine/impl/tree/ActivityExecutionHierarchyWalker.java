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

import java.util.Map;

import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Combination of flow scope and execution walker. Walks the flow scope
 * hierarchy upwards from the given execution to the top level process instance.
 *
 * @author Philipp Ossler
 *
 */
public class ActivityExecutionHierarchyWalker extends SingleReferenceWalker<ActivityExecutionTuple> {

  private Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping;

  public ActivityExecutionHierarchyWalker(ActivityExecution execution) {
    super(createTupel(execution));

    activityExecutionMapping = execution.createActivityExecutionMapping();
  }

  @Override
  protected ActivityExecutionTuple nextElement() {
    PvmScope currentScope = getCurrentElement().getScope();
    PvmScope flowScope = currentScope.getFlowScope();

    if (flowScope != null) {
      // walk to parent scope
      PvmExecutionImpl execution = activityExecutionMapping.get(flowScope);
      return new ActivityExecutionTuple(flowScope, execution);

    } else {
      // this is the process instance, look for parent
      PvmExecutionImpl currentExecution = activityExecutionMapping.get(currentScope);
      PvmExecutionImpl superExecution = currentExecution.getSuperExecution();

      if (superExecution != null) {
        // walk to parent process instance
        activityExecutionMapping = superExecution.createActivityExecutionMapping();
        return createTupel(superExecution);

      } else {
        // this is the top level process instance
        return null;
      }
    }
  }

  protected static ActivityExecutionTuple createTupel(ActivityExecution execution) {
    PvmScope flowScope = getCurrentFlowScope(execution);
    return new ActivityExecutionTuple(flowScope, execution);
  }

  protected static PvmScope getCurrentFlowScope(ActivityExecution execution) {
    ScopeImpl scope = null;
    if(execution.getTransition() != null) {
      scope = execution.getTransition().getDestination().getFlowScope();
    }
    else {
      scope = (ScopeImpl) execution.getActivity();
    }

    if (scope.isScope()) {
      return scope;
    } else {
      return scope.getFlowScope();
    }
  }

  public ReferenceWalker<ActivityExecutionTuple> addScopePreVisitor(TreeVisitor<PvmScope> visitor) {
    return addPreVisitor(new ScopeVisitorWrapper(visitor));
  }

  public ReferenceWalker<ActivityExecutionTuple> addScopePostVisitor(TreeVisitor<PvmScope> visitor) {
    return addPostVisitor(new ScopeVisitorWrapper(visitor));
  }

  public ReferenceWalker<ActivityExecutionTuple> addExecutionPreVisitor(TreeVisitor<ActivityExecution> visitor) {
    return addPreVisitor(new ExecutionVisitorWrapper(visitor));
  }

  public ReferenceWalker<ActivityExecutionTuple> addExecutionPostVisitor(TreeVisitor<ActivityExecution> visitor) {
    return addPostVisitor(new ExecutionVisitorWrapper(visitor));
  }

  private class ExecutionVisitorWrapper implements TreeVisitor<ActivityExecutionTuple> {

    private final TreeVisitor<ActivityExecution> collector;

    public ExecutionVisitorWrapper(TreeVisitor<ActivityExecution> collector) {
      this.collector = collector;
    }

    @Override
    public void visit(ActivityExecutionTuple tupel) {
      collector.visit(tupel.getExecution());
    }
  }

  private class ScopeVisitorWrapper implements TreeVisitor<ActivityExecutionTuple> {

    private final TreeVisitor<PvmScope> collector;

    public ScopeVisitorWrapper(TreeVisitor<PvmScope> collector) {
      this.collector = collector;
    }

    @Override
    public void visit(ActivityExecutionTuple tupel) {
      collector.visit(tupel.getScope());
    }
  }

}