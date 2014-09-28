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

import org.camunda.bpm.connect.Connector;
import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.ConnectorResponse;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.connector.ConnectorVariableScope;
import org.camunda.bpm.engine.impl.connector.Connectors;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Daniel Meyer
 *
 */
public class ServiceTaskConnectorActivityBehavior extends TaskActivityBehavior {

  /** the id of the connector */
  protected String connectorId;

  /** cached connector instance for this activity.
   * Will be initialized after the first execution of this activity. */
  protected Connector<ConnectorRequest<?>> connectorInstance;

  /** the local ioMapping for this connector. */
  protected IoMapping ioMapping;

  public ServiceTaskConnectorActivityBehavior(String connectorId, IoMapping ioMapping) {
    this.connectorId = connectorId;
    this.ioMapping = ioMapping;
  }

  public void execute(ActivityExecution execution) throws Exception {
    ensureConnectorInitialized();

    ConnectorRequest<? extends ConnectorResponse> request = connectorInstance.createRequest();

    // create a variable scopes for the connector
    ConnectorVariableScope connectorInputVariableScope = new ConnectorVariableScope((AbstractVariableScope) execution);
    ConnectorVariableScope connectorOuputVariableScope = new ConnectorVariableScope((AbstractVariableScope) execution);

    // TODO: make this non-optional?
    if(ioMapping != null) {
      // execute the connector input parameters
      ioMapping.executeInputParameters(connectorInputVariableScope);

      // write the local variables to the request.
      connectorInputVariableScope.writeToRequest(request);
    }

    try {
      // execute the request and obtain a response:
      ConnectorResponse response = request.execute();

      // TODO: make this non-optional?
      if(ioMapping != null) {
        // read parameters from response
        connectorOuputVariableScope.readFromResponse(response);

        // map variables to parent scope.
        ioMapping.executeOutputParameters(connectorOuputVariableScope);
      }

      // leave activity
      leave(execution);

    } catch(Exception e) {
      throw new ProcessEngineException("Exception while invoking connector "+e.getMessage(), e);
    }
  }

  protected void ensureConnectorInitialized() {
    if(connectorInstance == null) {
      synchronized (this) {
        if(connectorInstance == null) {
          Connectors connectors = Context.getProcessEngineConfiguration().getConnectors();
          connectorInstance = connectors.crateConnectorInstance(connectorId);
        }
      }
    }
  }

}
