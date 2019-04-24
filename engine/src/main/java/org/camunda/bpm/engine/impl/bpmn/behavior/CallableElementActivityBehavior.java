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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.concurrent.Callable;
import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;

import static org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior.NUMBER_OF_ACTIVE_INSTANCES;
import static org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior.NUMBER_OF_COMPLETED_INSTANCES;
import static org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior.NUMBER_OF_INSTANCES;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.util.ClassDelegateUtil;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CallableElementActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  protected String[] variablesFilter = { NUMBER_OF_INSTANCES, NUMBER_OF_ACTIVE_INSTANCES, NUMBER_OF_COMPLETED_INSTANCES };

  protected CallableElement callableElement;

  /**
   * The expression which identifies the delegation for the variable mapping.
   */
  protected Expression expression;

  /**
   * The class name of the delegated variable mapping, which should be used.
   */
  protected String className;

  public CallableElementActivityBehavior() {
  }

  public CallableElementActivityBehavior(String className) {
    this.className = className;
  }

  public CallableElementActivityBehavior(Expression expression) {
    this.expression = expression;
  }

  protected DelegateVariableMapping getDelegateVariableMapping(Object instance) {
    if (instance instanceof DelegateVariableMapping) {
      return (DelegateVariableMapping) instance;
    } else {
      throw LOG.missingDelegateVariableMappingParentClassException(
              instance.getClass().getName(),
              DelegateVariableMapping.class.getName());
    }
  }

  protected DelegateVariableMapping resolveDelegation(ActivityExecution execution) {
    Object delegate = resolveDelegateClass(execution);
    return delegate != null ? getDelegateVariableMapping(delegate) : null;
  }

  public Object resolveDelegateClass(final ActivityExecution execution) {
    ProcessApplicationReference targetProcessApplication
            = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);
    if (ProcessApplicationContextUtil.requiresContextSwitch(targetProcessApplication)) {
      return Context.executeWithinProcessApplication(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          return resolveDelegateClass(execution);
        }
      }, targetProcessApplication, new InvocationContext(execution));
    } else {
      return instantiateDelegateClass(execution);
    }
  }

  protected Object instantiateDelegateClass(ActivityExecution execution) {
    Object delegate = null;
    if (expression != null) {
      delegate = expression.getValue(execution);
    } else if (className != null) {
      delegate = ClassDelegateUtil.instantiateDelegate(className, null);
    }
    return delegate;
  }

  @Override
  public void execute(final ActivityExecution execution) throws Exception {
    final VariableMap variables = getInputVariables(execution);

    final DelegateVariableMapping varMapping = resolveDelegation(execution);
    if (varMapping != null) {
      invokeVarMappingDelegation(new DelegateInvocation(execution, null) {
        @Override
        protected void invoke() throws Exception {
          varMapping.mapInputVariables(execution, variables);
        }
      });
    }

    String businessKey = getBusinessKey(execution);
    startInstance(execution, variables, businessKey);
  }

  @Override
  public void passOutputVariables(final ActivityExecution execution, final VariableScope subInstance) {
    // only data. no control flow available on this execution.
    VariableMap variables = filterVariables(getOutputVariables(subInstance));
    VariableMap localVariables = getOutputVariablesLocal(subInstance);

    execution.setVariables(variables);
    execution.setVariablesLocal(localVariables);

    final DelegateVariableMapping varMapping = resolveDelegation(execution);
    if (varMapping != null) {
      invokeVarMappingDelegation(new DelegateInvocation(execution, null) {
        @Override
        protected void invoke() throws Exception {
          varMapping.mapOutputVariables(execution, subInstance);
        }
      });
    }
  }

  protected void invokeVarMappingDelegation(DelegateInvocation delegation) {
    try {
      Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(delegation);
    } catch (Exception ex) {
      throw new ProcessEngineException(ex);
    }
  }

  protected VariableMap filterVariables(VariableMap variables) {
    if (variables != null) {
      for (String key : variablesFilter) {
        variables.remove(key);
      }
    }
    return variables;
  }

  @Override
  public void completed(ActivityExecution execution) throws Exception {
    // only control flow. no sub instance data available
    leave(execution);
  }

  public CallableElement getCallableElement() {
    return callableElement;
  }

  public void setCallableElement(CallableElement callableElement) {
    this.callableElement = callableElement;
  }

  protected String getBusinessKey(ActivityExecution execution) {
    return getCallableElement().getBusinessKey(execution);
  }

  protected VariableMap getInputVariables(ActivityExecution callingExecution) {
    return getCallableElement().getInputVariables(callingExecution);
  }

  protected VariableMap getOutputVariables(VariableScope calledElementScope) {
    return getCallableElement().getOutputVariables(calledElementScope);
  }

  protected VariableMap getOutputVariablesLocal(VariableScope calledElementScope) {
    return getCallableElement().getOutputVariablesLocal(calledElementScope);
  }

  protected Integer getVersion(ActivityExecution execution) {
    return getCallableElement().getVersion(execution);
  }

  protected String getDeploymentId(ActivityExecution execution) {
    return getCallableElement().getDeploymentId();
  }

  protected CallableElementBinding getBinding() {
    return getCallableElement().getBinding();
  }

  protected boolean isLatestBinding() {
    return getCallableElement().isLatestBinding();
  }

  protected boolean isDeploymentBinding() {
    return getCallableElement().isDeploymentBinding();
  }

  protected boolean isVersionBinding() {
    return getCallableElement().isVersionBinding();
  }

  protected abstract void startInstance(ActivityExecution execution, VariableMap variables, String businessKey);

}
