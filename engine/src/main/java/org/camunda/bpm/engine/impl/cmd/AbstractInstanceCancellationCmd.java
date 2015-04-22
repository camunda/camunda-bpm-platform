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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractInstanceCancellationCmd extends AbstractProcessInstanceModificationCommand {

  public AbstractInstanceCancellationCmd(String processInstanceId) {
    super(processInstanceId);
  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntity sourceInstanceExecution = determineSourceInstanceExecution(commandContext);

    // Outline:
    // 1. find topmost scope execution beginning at scopeExecution that has exactly
    //    one child (this is the topmost scope we can cancel)
    // 2. cancel all children of the topmost execution
    // 3. cancel the activity of the topmost execution itself (if applicable)
    // 4. remove topmost execution (and concurrent parent) if topmostExecution is not the process instance

    ExecutionEntity topmostCancellableExecution = sourceInstanceExecution;
    ExecutionEntity parentScopeExecution = (ExecutionEntity) topmostCancellableExecution.getParentScopeExecution(false);

    // if topmostCancellabelExecution's parent is concurrent, we have reached the target execution
    while (parentScopeExecution != null && !topmostCancellableExecution.isConcurrent() && !topmostCancellableExecution.getParent().isConcurrent()) {
      topmostCancellableExecution = parentScopeExecution;
      parentScopeExecution = (ExecutionEntity) topmostCancellableExecution.getParentScopeExecution(false);
    }

    if (topmostCancellableExecution.isPreserveScope()) {
      topmostCancellableExecution.interrupt("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      topmostCancellableExecution.leaveActivityInstance();
      topmostCancellableExecution.setActivity(null);
    } else {
      topmostCancellableExecution.deleteCascade("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      handleChildRemovalInScope(topmostCancellableExecution);

    }

    return null;
  }

  protected void handleChildRemovalInScope(ExecutionEntity removedExecution) {
    // TODO: the following should be closer to PvmAtomicOperationDeleteCascadeFireActivityEnd
    // once CAM-3604 is fixed (note though that e.g. boundary events expect concurrent executions to be preserved)
    //
    // Idea: attempting to prune and synchronize on the parent is the default behavior when
    // a concurrent child is removed, but scope activities implementing ModificationObserverBehavior
    // override this default (and therefore *must* take care of reorganization themselves)

    // notify the behavior that a concurrent execution has been removed

    // must be set due to deleteCascade behavior
    ActivityImpl activity = removedExecution.getActivity();
    ScopeImpl flowScope = activity.getFlowScope();

    PvmExecutionImpl scopeExecution = removedExecution.getParentScopeExecution(false);
    PvmExecutionImpl scopeExecutionChild = removedExecution;
    PvmExecutionImpl parent = scopeExecutionChild.getParent();
    if (parent.isConcurrent()) {
      parent.remove();
      scopeExecutionChild = parent;
    }

    if (scopeExecutionChild.isConcurrent()) {
      if (flowScope.getActivityBehavior() != null
          && flowScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
        // let child removal be handled by the scope itself
        ModificationObserverBehavior behavior = (ModificationObserverBehavior) flowScope.getActivityBehavior();
        behavior.concurrentExecutionDeleted(scopeExecution, scopeExecutionChild);
      }
      else {
        // default: pruning
        scopeExecution.tryPruneLastConcurrentChild();
        scopeExecution.forceUpdate();
      }
    }
  }

  protected abstract ExecutionEntity determineSourceInstanceExecution(CommandContext commandContext);


}
