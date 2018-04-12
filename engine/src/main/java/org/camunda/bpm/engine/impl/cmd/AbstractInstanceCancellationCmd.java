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
import org.camunda.bpm.engine.impl.util.ModificationUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractInstanceCancellationCmd extends AbstractProcessInstanceModificationCommand {

  protected String cancellationReason;

  public AbstractInstanceCancellationCmd(String processInstanceId) {
    super(processInstanceId);
    this.cancellationReason = "Cancellation due to process instance modifcation";
  }

  public AbstractInstanceCancellationCmd(String processInstanceId, String cancellationReason) {
    super(processInstanceId);
    this.cancellationReason = cancellationReason;
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

    // if topmostCancellableExecution's scope execution has no other non-event-scope children,
    // we have reached the correct execution
    while (parentScopeExecution != null && (parentScopeExecution.getNonEventScopeExecutions().size() <= 1)) {
        topmostCancellableExecution = parentScopeExecution;
        parentScopeExecution = (ExecutionEntity) topmostCancellableExecution.getParentScopeExecution(false);
    }

    if (topmostCancellableExecution.isPreserveScope()) {
      topmostCancellableExecution.interrupt(cancellationReason, skipCustomListeners, skipIoMappings);
      topmostCancellableExecution.leaveActivityInstance();
      topmostCancellableExecution.setActivity(null);
    } else {
      topmostCancellableExecution.deleteCascade(cancellationReason, skipCustomListeners, skipIoMappings);
      ModificationUtil.handleChildRemovalInScope(topmostCancellableExecution);
    }

    return null;
  }

  protected abstract ExecutionEntity determineSourceInstanceExecution(CommandContext commandContext);

  protected ExecutionEntity findSuperExecution(ExecutionEntity parentScopeExecution, ExecutionEntity topmostCancellableExecution){
    ExecutionEntity superExecution = null;
    if(parentScopeExecution == null) {
      superExecution = topmostCancellableExecution.getSuperExecution();

    }
    return superExecution;
  }

}
