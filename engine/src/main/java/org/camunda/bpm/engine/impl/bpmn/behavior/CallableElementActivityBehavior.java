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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import static org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior.LOG;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.util.ClassDelegateUtil;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CallableElementActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  protected CallableElement callableElement;
  protected DelegateVariableMapping varMapping;

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

  protected void setDelegateVariableMapping(Object instance) {
    if (instance instanceof DelegateVariableMapping) {
      varMapping = (DelegateVariableMapping) instance;
    } else {
      throw LOG.missingDelegateVariableMappingParentClassException(
              instance.getClass().getName(),
              DelegateVariableMapping.class.getName());
    }
  }

  protected void resolveDelegation(ActivityExecution execution) {
    if (varMapping == null) {
      Object delegate = null;
      if (expression != null) {
        delegate = expression.getValue(execution);
        setDelegateVariableMapping(delegate);
      } else if (varMapping == null && className != null) {
        delegate = ClassDelegateUtil.instantiateDelegate(className, null);
      }
      if (delegate != null) {
        setDelegateVariableMapping(delegate);
      }
    }
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    VariableMap variables = getInputVariables(execution);
    resolveDelegation(execution);

    if (varMapping != null) {
      varMapping.mapInputVariables(execution, variables);
    }
    String businessKey = getBusinessKey(execution);
    startInstance(execution, variables, businessKey);
  }

  @Override
  public void passOutputVariables(ActivityExecution execution, VariableScope subInstance) {
    // only data. no control flow available on this execution.
    VariableMap variables = getOutputVariables(subInstance);
    VariableMap localVariables = getOutputVariablesLocal(subInstance);

    execution.setVariables(variables);
    execution.setVariablesLocal(localVariables);

    if (varMapping != null) {
      varMapping.mapOutputVariables(execution, subInstance);
    }
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
