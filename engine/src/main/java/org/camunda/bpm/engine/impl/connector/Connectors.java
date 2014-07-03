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
package org.camunda.bpm.engine.impl.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.connect.Connector;
import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.interceptor.RequestInterceptor;
import org.camunda.bpm.engine.ProcessEngineException;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * <p>Simple Registry and factory for connectors.</p>
 *
 * @author Daniel Meyer
 *
 */
public class Connectors {

  protected Map<String, Class<? extends Connector<?>>> registeredConnectors = new HashMap<String, Class<? extends Connector<?>>>();

  protected List<RequestInterceptor> globalRequestInterceptors = new ArrayList<RequestInterceptor>();

  public void addConnector(String id, Class<? extends Connector<?>> connector) {
    registeredConnectors.put(id, connector);
  }

  public Class<? extends Connector<?>> getConnector(String id) {
    return registeredConnectors.get(id);
  }

  @SuppressWarnings("unchecked")
  public <T extends Connector<ConnectorRequest<?>>> T crateConnectorInstance(String id) {
    Class<? extends Connector<?>> connector = getConnector(id);
    ensureNotNull("Cannot create instance of connector with id '" + id + "' : no such connector registered", "connector", connector);

    try {
      T connectorInstance = (T) connector.newInstance();
      // add request interceptors to connector instance.
      connectorInstance.getRequestInterceptors().addAll(globalRequestInterceptors);

      return connectorInstance;

    } catch (InstantiationException e) {
      throw new ProcessEngineException("Cound not create instance of connector '" + id + "': " + e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new ProcessEngineException("Cound not create instance of connector '" + id + "': " + e.getMessage(), e);
    }
  }

}
