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

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CallableElementActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  protected CallableElement callableElement;

  public void execute(ActivityExecution execution) throws Exception {
    VariableMap variables = getInputVariables(execution);
    String businessKey = getBusinessKey(execution);
    startInstance(execution, variables, businessKey);
  }

  public void completing(VariableScope execution, VariableScope subInstance) throws Exception {
    // only data. no control flow available on this execution.
    VariableMap variabes = getOutputVariables(subInstance);
    execution.setVariables(variabes);
  }

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

  protected VariableMap getInputVariables(ActivityExecution execution) {
    return getCallableElement().getInputVariables(execution);
  }

  protected VariableMap getOutputVariables(VariableScope variableScope) {
    return getCallableElement().getOutputVariables(variableScope);
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
