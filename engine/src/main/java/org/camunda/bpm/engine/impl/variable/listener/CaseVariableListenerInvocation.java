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
package org.camunda.bpm.engine.impl.variable.listener;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;

/**
 * @author Thorben Lindhauer
 *
 */
public class CaseVariableListenerInvocation extends DelegateInvocation {

  protected CaseVariableListener variableListenerInstance;
  protected DelegateCaseVariableInstance variableInstance;

  public CaseVariableListenerInvocation(CaseVariableListener variableListenerInstance, DelegateCaseVariableInstance variableInstance) {
    this(variableListenerInstance, variableInstance, null);
  }

  public CaseVariableListenerInvocation(CaseVariableListener variableListenerInstance, DelegateCaseVariableInstance variableInstance,
      BaseDelegateExecution contextExecution) {
    this.variableListenerInstance = variableListenerInstance;
    this.variableInstance = variableInstance;
    this.contextExecution = contextExecution;
  }

  protected void invoke() throws Exception {
    try {
      if (isCaseExecution()) {
        Context.setExecutionContext((CaseExecutionEntity) contextExecution);
      }
      variableListenerInstance.notify(variableInstance);
    }
    finally {
      if (isCaseExecution()) {
        Context.removeExecutionContext();
      }
    }
  }

  protected boolean isCaseExecution() {
    return contextExecution != null && contextExecution instanceof CaseExecutionEntity;
  }

  public Object getTarget() {
    return variableListenerInstance;
  }

}
