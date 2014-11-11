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
package org.camunda.connect.plugin;

import java.lang.Object;import java.lang.String;import java.util.Map;
import java.util.Map.Entry;

import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorResponse;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.CoreVariableStore;

/**
 * Exposes a connector request as variableScope.
 *
 * @author Daniel Meyer
 *
 */
public class ConnectorVariableScope extends AbstractVariableScope {

  private static final long serialVersionUID = 1L;

  protected AbstractVariableScope parent;

  protected ConnectorVariableStore variableStore;

  public ConnectorVariableScope(AbstractVariableScope parent) {
    this.parent = parent;
    this.variableStore = new ConnectorVariableStore();
  }

  public String getVariableScopeKey() {
    return "connector";
  }

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  public AbstractVariableScope getParentVariableScope() {
    return parent;
  }

  public void writeToRequest(ConnectorRequest<?> request) {
    for (CoreVariableInstance variable : variableStore.getVariableInstancesValues()) {
      request.setRequestParameter(variable.getName(), variable.getTypedValue(true).getValue());
    }
  }

  public void readFromResponse(ConnectorResponse response) {
    Map<String, Object> responseParameters = response.getResponseParameters();
    for (Entry<String, Object> entry : responseParameters.entrySet()) {
      setVariableLocal(entry.getKey(), entry.getValue());
    }
  }

}
