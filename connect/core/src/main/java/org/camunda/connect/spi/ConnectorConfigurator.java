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
package org.camunda.connect.spi;

/**
 * Can be used to configure connectors of a certain type.
 * An implementation will be supplied with all discovered
 * connectors of the specified class. {@link Connector#getId()}
 * may be used for further restrict configuration to connectors
 * with a specific id.
 */
public interface ConnectorConfigurator<C extends Connector<?>> {

  /**
   * @return the class of connectors this configurator can configure (including subclasses)
   */
  Class<C> getConnectorClass();

  /**
   * Configures the connector instance. This method is invoked with
   * all connectors of the specified class.
   *
   * @param connector the connector instance to configure
   */
  void configure(C connector);

}
