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
package org.camunda.bpm.engine.impl.task.delegate;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.CoreExecutionContext;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Class handling invocations of {@link TaskListener TaskListeners}
 *
 * @author Daniel Meyer
 */
public class TaskListenerInvocation extends DelegateInvocation {

  protected final TaskListener executionListenerInstance;
  protected final DelegateTask delegateTask;

  public TaskListenerInvocation(TaskListener executionListenerInstance, DelegateTask delegateTask) {
    this(executionListenerInstance, delegateTask, null);
  }

  public TaskListenerInvocation(TaskListener executionListenerInstance, DelegateTask delegateTask, BaseDelegateExecution contextExecution) {
    this.executionListenerInstance = executionListenerInstance;
    this.delegateTask = delegateTask;
    this.contextExecution = contextExecution;
  }

  protected void invoke() throws Exception {
    CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    try {
      if (executionContext == null) {
        if (contextExecution instanceof ExecutionEntity) {
          Context.setExecutionContext((ExecutionEntity) contextExecution);
        } else {
          Context.setExecutionContext((CaseExecutionEntity) contextExecution);
        }
      }
      executionListenerInstance.notify(delegateTask);
    }
    finally {
      if (executionContext == null) {
        Context.removeExecutionContext();
      }
    }
  }

  public Object getTarget() {
    return executionListenerInstance;
  }

}
