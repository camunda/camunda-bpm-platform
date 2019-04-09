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
package org.camunda.connect.plugin.impl;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.connect.ConnectorException;
import org.camunda.connect.Connectors;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorResponse;

/**
 * @author Daniel Meyer
 *
 */
public class ServiceTaskConnectorActivityBehavior extends TaskActivityBehavior {

  /** the id of the connector */
  protected String connectorId;

  /** cached connector instance for this activity.
   * Will be initialized after the first execution of this activity. */
  protected Connector<?> connectorInstance;

  /** the local ioMapping for this connector. */
  protected IoMapping ioMapping;

  public ServiceTaskConnectorActivityBehavior(String connectorId, IoMapping ioMapping) {
    this.connectorId = connectorId;
    this.ioMapping = ioMapping;
  }

  public void execute(final ActivityExecution execution) throws Exception {
    ensureConnectorInitialized();

    executeWithErrorPropagation(execution, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ConnectorRequest<?> request = connectorInstance.createRequest();
        applyInputParameters(execution, request);
        // execute the request and obtain a response:
        ConnectorResponse response = request.execute();
        applyOutputParameters(execution, response);
        leave(execution);
        return null;
      }
    });

  }

  protected void applyInputParameters(ActivityExecution execution, ConnectorRequest<?> request) {
    if(ioMapping != null) {
      // create variable scope for input parameters
      ConnectorVariableScope connectorInputVariableScope = new ConnectorVariableScope((AbstractVariableScope) execution);
      // execute the connector input parameters
      ioMapping.executeInputParameters(connectorInputVariableScope);
      // write the local variables to the request.
      connectorInputVariableScope.writeToRequest(request);
    }
  }

  protected void applyOutputParameters(ActivityExecution execution, ConnectorResponse response) {
    if(ioMapping != null) {
      // create variable scope for output parameters
      ConnectorVariableScope connectorOutputVariableScope = new ConnectorVariableScope((AbstractVariableScope) execution);
      // read parameters from response
      connectorOutputVariableScope.readFromResponse(response);
      // map variables to parent scope.
      ioMapping.executeOutputParameters(connectorOutputVariableScope);
    }
  }

  protected void ensureConnectorInitialized() {
    if(connectorInstance == null) {
      synchronized (this) {
        if(connectorInstance == null) {
          connectorInstance = Connectors.getConnector(connectorId);
          if (connectorInstance == null) {
            throw new ConnectorException("No connector found for connector id '" + connectorId + "'");
          }
        }
      }
    }
  }

}
