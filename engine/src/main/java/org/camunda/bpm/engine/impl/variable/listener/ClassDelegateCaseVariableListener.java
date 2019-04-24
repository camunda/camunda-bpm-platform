/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.variable.listener;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.instantiateDelegate;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ClassDelegate;

/**
 * @author Thorben Lindhauer
 *
 */
public class ClassDelegateCaseVariableListener extends ClassDelegate implements CaseVariableListener {

  public ClassDelegateCaseVariableListener(String className, List<FieldDeclaration> fieldDeclarations) {
    super(className, fieldDeclarations);
  }

  public ClassDelegateCaseVariableListener(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    super(clazz, fieldDeclarations);
  }

  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    CaseVariableListener variableListenerInstance = getVariableListenerInstance();

    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new CaseVariableListenerInvocation(variableListenerInstance, variableInstance));
  }

  protected CaseVariableListener getVariableListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof CaseVariableListener) {
      return (CaseVariableListener) delegateInstance;

    } else {
      throw new ProcessEngineException(delegateInstance.getClass().getName()+" doesn't implement "+CaseVariableListener.class);
    }
  }

}
