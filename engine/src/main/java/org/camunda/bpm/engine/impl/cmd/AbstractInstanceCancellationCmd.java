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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

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
    ExecutionEntity parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);

    // if topmostCancellabelExecution's parent is concurrent, we have reached the target execution
    while (parentScopeExecution != null && !topmostCancellableExecution.isConcurrent() && !topmostCancellableExecution.getParent().isConcurrent()) {
      topmostCancellableExecution = parentScopeExecution;
      parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);
    }

    if (topmostCancellableExecution.isProcessInstanceExecution()) {
      topmostCancellableExecution.interrupt("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      topmostCancellableExecution.leaveActivityInstance();
      topmostCancellableExecution.setActivity(null);
    } else {
      topmostCancellableExecution.deleteCascade("Cancelled due to process instance modification", skipCustomListeners, skipIoMappings);
      topmostCancellableExecution.removeFromParentScope();

    }

    return null;
  }

  protected abstract ExecutionEntity determineSourceInstanceExecution(CommandContext commandContext);

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
