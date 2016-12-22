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
package org.camunda.bpm.engine.impl.core.variable.scope;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 * @author Ryan Johnston
 *
 */
public class VariableListenerInvocationListener implements VariableInstanceLifecycleListener<VariableInstanceEntity> {

  protected final AbstractVariableScope targetScope;

  public VariableListenerInvocationListener(AbstractVariableScope targetScope) {
    this.targetScope = targetScope;
  }

  @Override
  public void onCreate(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    handleEvent(new VariableEvent(variable, VariableListener.CREATE, sourceScope));
  }

  @Override
  public void onUpdate(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    handleEvent(new VariableEvent(variable, VariableListener.UPDATE, sourceScope));
  }

  @Override
  public void onDelete(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    handleEvent(new VariableEvent(variable, VariableListener.DELETE, sourceScope));
  }

  protected void handleEvent(VariableEvent event) {
    AbstractVariableScope sourceScope = event.getSourceScope();

    if (sourceScope instanceof ExecutionEntity) {
      addEventToScopeExecution((ExecutionEntity) sourceScope, event);
    } else if (sourceScope instanceof TaskEntity) {
      TaskEntity task = (TaskEntity) sourceScope;
      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        addEventToScopeExecution(execution, event);
      }
    } else if(sourceScope.getParentVariableScope() instanceof ExecutionEntity) {
      addEventToScopeExecution((ExecutionEntity)sourceScope.getParentVariableScope(), event);
    }
    else {
      throw new ProcessEngineException("BPMN execution scope expected");
    }
  }

  protected void addEventToScopeExecution(ExecutionEntity sourceScope, VariableEvent event) {

    // ignore events of variables that are not set in an execution
    ExecutionEntity sourceExecution = sourceScope;
    ExecutionEntity scopeExecution = sourceExecution.isScope() ? sourceExecution : sourceExecution.getParent();
    scopeExecution.delayEvent((ExecutionEntity) targetScope, event);

  }
}
