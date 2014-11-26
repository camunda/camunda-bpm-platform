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
package org.camunda.bpm.engine.impl.cmmn.listener;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.instantiateDelegate;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.delegate.CaseExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ClassDelegate;

/**
 * @author Roman Smirnov
 *
 */
public class ClassDelegateCaseExecutionListener extends ClassDelegate implements CaseExecutionListener {

  public ClassDelegateCaseExecutionListener(String className, List<FieldDeclaration> fieldDeclarations) {
    super(className, fieldDeclarations);
  }

  public ClassDelegateCaseExecutionListener(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    super(clazz, fieldDeclarations);
  }

  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    CaseExecutionListener listenerInstance = getListenerInstance();

    Context
      .getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new CaseExecutionListenerInvocation(listenerInstance, caseExecution));
  }

  protected CaseExecutionListener getListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof CaseExecutionListener) {
      return (CaseExecutionListener) delegateInstance;
    } else {
      throw new ProcessEngineException(delegateInstance.getClass().getName()+" doesn't implement "+CaseExecutionListener.class);
    }
  }


}
