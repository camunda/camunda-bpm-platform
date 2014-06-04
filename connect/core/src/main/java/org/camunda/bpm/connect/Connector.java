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
package org.camunda.bpm.connect;

import java.util.List;

import org.camunda.bpm.connect.interceptor.RequestInterceptor;

/**
 * <p>A connector represents a way to interact with some system, library or technology.
 * Examples of connectors are connectors for HTTP interaction, a connector to a
 * third-party library like a rules engine or file system abstraction.</p>
 *
 * <p>Once an instance of a connector is created, it is thread-safe, meaning that single
 * connector instance my be used by multiple threads concurrently. Each thread will create
 * new {@link ConnectorRequest Requests} which are not threadsafe and must not be shared
 * among multiple threads.</p>
 *
 * <p>Connectors support interceptors. Interceptors may be used for customizing connector
 * behavior or for adding management aspects such as logging to a connector.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface Connector<T extends ConnectorRequest<?>> {

  /**
   * The unique Id for the connector.
   *
   * @return the unique connector id
   */
  String getId();

  /**
   * Create a request on the connector.
   *
   * @return the connector-specific request object.
   */
  T createRequest();

  /**
   * Adds a {@link RequestInterceptor} to this connector. The interceptor is added
   * at the end of the interceptor chain. The interceptor is invoked for all requests
   * created by the connector.
   */
  void addRequestInterceptor(RequestInterceptor interceptor);

  /**
   * Returns the {@link RequestInterceptor} chain for this connector.
   * The implementation will return the actual list, modifications on the list will
   * be reflected in the internal connector state. This means that you can use the
   * returned list to modify the connector's {@link RequestInterceptor} chain.
   *
   * @return the list of interceptors.
   */
  List<RequestInterceptor> getRequestInterceptors();

}
