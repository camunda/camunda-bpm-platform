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

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CallableElement extends BaseCallableElement {

  protected ParameterValueProvider businessKeyValueProvider;
  protected List<CallableElementParameter> inputs;
  protected List<CallableElementParameter> outputs;
  protected List<CallableElementParameter> outputsLocal;

  public CallableElement() {
    this.inputs = new ArrayList<CallableElementParameter>();
    this.outputs = new ArrayList<CallableElementParameter>();
    this.outputsLocal = new ArrayList<CallableElementParameter>();
  }

  // definitionKey ////////////////////////////////////////////////////////////////

  // binding /////////////////////////////////////////////////////////////////////

  // version //////////////////////////////////////////////////////////////////////

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

  public List<CallableElementParameter> getOutputsLocal() {
    return outputsLocal;
  }

  public void addOutput(CallableElementParameter output) {
    outputs.add(output);
  }

  public void addOutputLocal(CallableElementParameter output) {
    outputsLocal.add(output);
  }

  public void addOutputs(List<CallableElementParameter> outputs) {
    this.outputs.addAll(outputs);
  }

  public VariableMap getOutputVariables(VariableScope calledElementScope) {
    List<CallableElementParameter> outputs = getOutputs();
    return getVariables(outputs, calledElementScope);
  }

  public VariableMap getOutputVariablesLocal(VariableScope calledElementScope) {
    List<CallableElementParameter> outputs = getOutputsLocal();
    return getVariables(outputs, calledElementScope);
  }

  // variables //////////////////////////////////////////////////////////////////

  protected VariableMap getVariables(List<CallableElementParameter> params, VariableScope variableScope) {
    VariableMap result = Variables.createVariables();

    for (CallableElementParameter param : params) {
      param.applyTo(variableScope, result);
    }

    return result;
  }

  // deployment id //////////////////////////////////////////////////////////////

}
