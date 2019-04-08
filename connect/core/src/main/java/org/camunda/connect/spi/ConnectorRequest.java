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

import java.util.Map;

/**
 * <p>A connector request. The request opens an interaction with the connector.
 * Because a request represents a single interaction, the request is not thread-safe.
 * Request objects should not be shared between multiple threads.</p>
 *
 * <p>The parameters of a request can be provided using the generic map of input
 * parameters. See ({@link #setRequestParameters(Map)}):
 * <pre>
 *  SomeConnectorRequest req = connector.createRequest();
 *  req.setRequestParameter("endpointUrl", "http://mysystem.loc/foo");
 *  req.setRequestParameter("payload", "some important payload");
 *  req.execute();
 * </pre>
 * This makes it possible to use the connector in a generic / configurable system like
 * the camunda process engine.
 * </p>
 *
 * <p>Optionally, a connector may also extend the request interface and provide
 * dedicated (type-safe) methods for configuring a request, preferably in a
 * fluent API fashion:
 * <pre>
 *  connector.createRequest()
 *    .endpointUrl("http://mysystem.loc/foo")
 *    .payload("some important payload")
 *    .execute();
 * </pre>
 * This makes it easy to use the connector in a standalone way.
 * </p>
 *
 * <p>A request must return a {@link ConnectorResponse} that contains response
 * data. Requests for which the response contains no payload or is not relevant should
 * return an empty response that provides no data.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ConnectorRequest<R extends ConnectorResponse> {

  /**
   * Provides the named input parameters of the request.
   *
   * @param params the named input parameters of the request.
   */
  void setRequestParameters(Map<String, Object> params);

  /**
   * Provides a named input parameters to the request.
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  void setRequestParameter(String name, Object value);

  /**
   * Returns the map of request parameters
   * @return the map of request parameters
   */
  Map<String, Object> getRequestParameters();

  /**
   * Returns the value of a request parameter
   * @param name the name of the request parameter
   * @return the value of the request parameter of 'null' if the parameter is not set.
   */
  <V> V getRequestParameter(String name);

  /**
   * Execute the request. Once a request is configured with all input
   * parameters, it can be executed.
   *
   * @return the return value of the request.
   */
  R execute();

}
