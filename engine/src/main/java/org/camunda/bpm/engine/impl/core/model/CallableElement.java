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
package org.camunda.bpm.engine.impl.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CallableElement {

  protected ParameterValueProvider definitionKeyValueProvider;
  protected CallableElementBinding binding;
  protected ParameterValueProvider versionValueProvider;
  protected ParameterValueProvider businessKeyValueProvider;
  protected List<CallableElementParameter> inputs;
  protected List<CallableElementParameter> outputs;
  protected String deploymentId;

  public enum CallableElementBinding {
    LATEST("latest"),
    DEPLOYMENT("deployment"),
    VERSION("version");

    private String value;

    private CallableElementBinding(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public CallableElement() {
    this.inputs = new ArrayList<CallableElementParameter>();
    this.outputs = new ArrayList<CallableElementParameter>();
  }

  // definitionKey ////////////////////////////////////////////////////////////////

  public String getDefinitionKey(VariableScope variableScope) {
    Object result = definitionKeyValueProvider.getValue(variableScope);

    if (result != null && !(result instanceof String)) {
      throw new ClassCastException("Cannot cast '"+result+"' to String");
    }

    return (String) result;
  }

  public ParameterValueProvider getDefinitionKeyValueProvider() {
    return definitionKeyValueProvider;
  }

  public void setDefinitionKeyValueProvider(ParameterValueProvider definitionKey) {
    this.definitionKeyValueProvider = definitionKey;
  }

  // binding /////////////////////////////////////////////////////////////////////

  public CallableElementBinding getBinding() {
    return binding;
  }

  public void setBinding(CallableElementBinding binding) {
    this.binding = binding;
  }

  public boolean isLatestBinding() {
    CallableElementBinding binding = getBinding();
    return binding == null || CallableElementBinding.LATEST.equals(binding);
  }

  public boolean isDeploymentBinding() {
    CallableElementBinding binding = getBinding();
    return CallableElementBinding.DEPLOYMENT.equals(binding);
  }

  public boolean isVersionBinding() {
    CallableElementBinding binding = getBinding();
    return CallableElementBinding.VERSION.equals(binding);
  }

  // version //////////////////////////////////////////////////////////////////////

  public Integer getVersion(VariableScope variableScope) {
    Object result = versionValueProvider.getValue(variableScope);

    if (result != null) {
      if (result instanceof String) {
        return Integer.valueOf((String) result);
      } else if (result instanceof Integer) {
        return (Integer) result;
      } else {
        throw new ProcessEngineException("It is not possible to transform '"+result+"' into an integer.");
      }
    }

    return null;
  }

  public ParameterValueProvider getVersionValueProvider() {
    return versionValueProvider;
  }

  public void setVersionValueProvider(ParameterValueProvider version) {
    this.versionValueProvider = version;
  }

  // businessKey /////////////////////////////////////////////////////////////////

  public String getBusinessKey(VariableScope variableScope) {
    if (businessKeyValueProvider == null) {
      return null;
    }

    Object result = businessKeyValueProvider.getValue(variableScope);

    if (result != null && !(result instanceof String)) {
      throw new ClassCastException("Cannot cast '"+result+"' to String");
    }

    return (String) result;
  }

  public ParameterValueProvider getBusinessKeyValueProvider() {
    return businessKeyValueProvider;
  }

  public void setBusinessKeyValueProvider(ParameterValueProvider businessKeyValueProvider) {
    this.businessKeyValueProvider = businessKeyValueProvider;
  }

  // inputs //////////////////////////////////////////////////////////////////////

  public List<CallableElementParameter> getInputs() {
    return inputs;
  }

  public void addInput(CallableElementParameter input) {
    inputs.add(input);
  }

  public void addInputs(List<CallableElementParameter> inputs) {
    this.inputs.addAll(inputs);
  }

  public VariableMap getInputVariables(VariableScope variableScope) {
    List<CallableElementParameter> inputs = getInputs();
    return getVariables(inputs, variableScope);
  }

  // outputs /////////////////////////////////////////////////////////////////////

  public List<CallableElementParameter> getOutputs() {
    return outputs;
  }

  public void addOutput(CallableElementParameter output) {
    outputs.add(output);
  }

  public void addOutputs(List<CallableElementParameter> outputs) {
    this.outputs.addAll(outputs);
  }

  public VariableMap getOutputVariables(VariableScope variableScope) {
    List<CallableElementParameter> outputs = getOutputs();
    return getVariables(outputs, variableScope);
  }

  // variables //////////////////////////////////////////////////////////////////

  protected VariableMap getVariables(List<CallableElementParameter> params, VariableScope variableScope) {
    VariableMap result = Variables.createVariables();

    for (CallableElementParameter param : params) {

      if (param.isAllVariables()) {
        Map<String, Object> allVariables = variableScope.getVariables();
        result.putAll(allVariables);

      } else {
        String targetVariableName = param.getTarget();
        Object value = param.getSource(variableScope);
        result.put(targetVariableName, value);
      }

    }

    return result;
  }

  // deployment id //////////////////////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

}
