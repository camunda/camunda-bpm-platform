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

import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableScopeLocalAdapter;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public class CallableElementParameter {

  protected ParameterValueProvider sourceValueProvider;
  protected String target;
  protected boolean allVariables;
  protected boolean readLocal = false;

  // source ////////////////////////////////////////////////////////

  public Object getSource(VariableScope variableScope) {

    if (sourceValueProvider instanceof ConstantValueProvider) {
      String variableName = (String) sourceValueProvider.getValue(variableScope);

      return variableScope.getVariableTyped(variableName);
    }
    else {
      return sourceValueProvider.getValue(variableScope);
    }

  }

  public void applyTo(VariableScope variableScope, VariableMap variables) {
    if (readLocal) {
      variableScope = new VariableScopeLocalAdapter(variableScope);
    }

    if (allVariables) {
      Map<String, Object> allVariables = variableScope.getVariables();
      variables.putAll(allVariables);

    } else {
      Object value = getSource(variableScope);
      variables.put(target, value);
    }
  }

  public ParameterValueProvider getSourceValueProvider() {
    return sourceValueProvider;
  }

  public void setSourceValueProvider(ParameterValueProvider source) {
    this.sourceValueProvider = source;
  }

  // target //////////////////////////////////////////////////////////

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  // all variables //////////////////////////////////////////////////

  public boolean isAllVariables() {
    return allVariables;
  }

  public void setAllVariables(boolean allVariables) {
    this.allVariables = allVariables;
  }

  // local

  public void setReadLocal(boolean readLocal) {
    this.readLocal = readLocal;
  }

  public boolean isReadLocal() {
    return readLocal;
  }

}
