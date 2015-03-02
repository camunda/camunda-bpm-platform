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

import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstanceCancellationCmd extends AbstractProcessInstanceModificationCommand {

  protected String activityInstanceId;

  public ActivityInstanceCancellationCmd(String processInstanceId, String activityInstanceId) {
    super(processInstanceId);
    this.activityInstanceId = activityInstanceId;

  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    // rebuild the mapping because the execution tree changes with every iteration
    ActivityExecutionMapping mapping = new ActivityExecutionMapping(commandContext, processInstanceId);

    ActivityInstance instance = new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
    ActivityInstance instanceToCancel = findActivityInstance(instance, activityInstanceId);
    ExecutionEntity scopeExecution = getScopeExecutionForActivityInstance(processInstance, mapping, instanceToCancel);

    // Outline:
    // 1. find topmost scope execution beginning at scopeExecution that has exactly
    //    one child (this is the topmost scope we can cancel)
    // 2. cancel all children of the topmost execution
    // 3. cancel the activity of the topmost execution itself (if applicable)
    // 4. remove topmost execution (and concurrent parent) if topmostExecution is not the process instance

    ExecutionEntity topmostCancellableExecution = scopeExecution;
    ExecutionEntity parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);

    // if topmostCancellabelExecution's parent is concurrent, we have reached the target execution
    while (parentScopeExecution != null && !topmostCancellableExecution.isConcurrent() && !topmostCancellableExecution.getParent().isConcurrent()) {
      topmostCancellableExecution = parentScopeExecution;
      parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);
    }

    if (topmostCancellableExecution.isProcessInstanceExecution()) {
      topmostCancellableExecution.cancelScope("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      // TODO: the following instruction should go into #cancelScope but this breaks some things like
      // transaction subprocesses
      topmostCancellableExecution.leaveActivityInstance();
      topmostCancellableExecution.setActivity(null);
    } else {
      topmostCancellableExecution.deleteCascade("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      topmostCancellableExecution.removeFromParentScope();

    }

    return null;
  }

  protected ExecutionEntity getParentScopeExecution(ExecutionEntity execution) {
    ExecutionEntity parent = execution.getParent();
    if (parent == null) {
      return null;
    }

    if (!parent.isScope()) {
      parent = parent.getParent();
    }
    return parent;
  }


}
