/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.connect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorConfigurator;
import org.camunda.connect.spi.ConnectorProvider;
import org.camunda.connect.spi.ConnectorRequest;

/**
 * Provides access to all available connectors.
 */
public class Connectors {

  protected static ConnectCoreLogger LOG = ConnectLogger.CORE_LOGGER;

  public static String HTTP_CONNECTOR_ID = "http-connector";
  public static String SOAP_HTTP_CONNECTOR_ID = "soap-http-connector";

  /** The global instance of the manager */
  static Connectors INSTANCE = new Connectors();

  /**
   * Provides the global instance of the Connectors manager.
   * @return the global instance
   */
  public static Connectors getInstance() {
    return INSTANCE;
  }

  /**
   * @return the connector for the default http connector id or null if
   * no connector is registered for this id
   */
  @SuppressWarnings("unchecked")
  public static <C extends Connector<? extends ConnectorRequest<?>>> C http() {
    return (C) INSTANCE.getConnectorById(HTTP_CONNECTOR_ID);
  }

  /**
   * @return the connector for the default soap http connector id or null
   * if no connector is registered for this id
   */
  @SuppressWarnings("unchecked")
  public static <C extends Connector<? extends ConnectorRequest<?>>> C soap() {
    return (C) INSTANCE.getConnectorById(SOAP_HTTP_CONNECTOR_ID);
  }

  /**
   * @return the connector for the given id or null if no connector is
   * registered for this id
   */
  @SuppressWarnings("unchecked")
  public static <C extends Connector<? extends ConnectorRequest<?>>> C getConnector(String connectorId) {
    return (C) INSTANCE.getConnectorById(connectorId);
  }

  /**
   * @return all register connectors
   */
  public static Set<Connector<? extends ConnectorRequest<?>>> getAvailableConnectors() {
    return INSTANCE.getAllAvailableConnectors();
  }

  // instance //////////////////////////////////////////////////////////

  protected Map<String, Connector<?>> availableConnectors;

  /**
   * @return all register connectors
   */
  public Set<Connector<? extends ConnectorRequest<?>>> getAllAvailableConnectors() {
    ensureConnectorProvidersInitialized();
    return new HashSet<Connector<?>>(availableConnectors.values());
  }

  /**
   * @return the connector for the given id or null if no connector is
   * registered for this id
   */
  @SuppressWarnings("unchecked")
  public <C extends Connector<? extends ConnectorRequest<?>>> C getConnectorById(String connectorId) {
    ensureConnectorProvidersInitialized();
    return (C) availableConnectors.get(connectorId);
  }

  /**
   * Detect all available connectors in the classpath using a {@link ServiceLoader}.
   */
  protected void ensureConnectorProvidersInitialized() {
    if (availableConnectors == null) {
      synchronized (Connectors.class) {
        if (availableConnectors == null) {
          initializeConnectors();
        }
      }
    }
  }

  protected void initializeConnectors() {
    Map<String, Connector<?>> connectors = new HashMap<String, Connector<?>>();

    // discover available custom connector providers on the classpath
    registerConnectors(connectors);

    // discover and apply connector configurators on the classpath
    applyConfigurators(connectors);

    this.availableConnectors = connectors;

  }

  protected void registerConnectors(Map<String, Connector<?>> connectors) {
    ServiceLoader<ConnectorProvider> providers = ServiceLoader.load(ConnectorProvider.class, Connectors.class.getClassLoader());

    for (ConnectorProvider provider : providers) {
      registerProvider(connectors, provider);
    }
  }

  protected void registerProvider(Map<String, Connector<?>> connectors, ConnectorProvider provider)  {
    String connectorId = provider.getConnectorId();
    if (connectors.containsKey(connectorId)) {
      throw LOG.multipleConnectorProvidersFound(connectorId);
    }
    else {
      Connector<?> connectorInstance = provider.createConnectorInstance();
      LOG.connectorProviderDiscovered(provider, connectorId, connectorInstance);
      connectors.put(connectorId, connectorInstance);
    }
  }

  @SuppressWarnings("rawtypes")
  protected void applyConfigurators(Map<String, Connector<?>> connectors) {
    ServiceLoader<ConnectorConfigurator> configurators = ServiceLoader.load(ConnectorConfigurator.class, Connectors.class.getClassLoader());

    for (ConnectorConfigurator configurator : configurators) {
      LOG.connectorConfiguratorDiscovered(configurator);
      applyConfigurator(connectors, configurator);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void applyConfigurator(Map<String, Connector<?>> connectors, ConnectorConfigurator configurator) {
    for (Connector<?> connector : connectors.values()) {
      if (configurator.getConnectorClass().isAssignableFrom(connector.getClass())) {
        configurator.configure(connector);
      }
    }
  }

}
