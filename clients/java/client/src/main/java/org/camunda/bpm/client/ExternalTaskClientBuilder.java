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
package org.camunda.bpm.client;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

/**
 * <p>A fluent builder to configure the Camunda client</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTaskClientBuilder {

  /**
   * Base url of the Camunda BPM Platform REST API. This information is mandatory.
   *
   * @param baseUrl of the Camunda BPM Platform REST API
   * @return the builder
   */
  ExternalTaskClientBuilder baseUrl(String baseUrl);

  /**
   * A custom worker id the Workflow Engine is aware of. This information is optional.
   * Note: make sure to choose a unique worker id
   *
   * If not given or null, a worker id is generated automatically which consists of the
   * hostname as well as a random and unique 128 bit string (UUID).
   *
   * @param workerId the Workflow Engine is aware of
   * @return the builder
   */
  ExternalTaskClientBuilder workerId(String workerId);

  /**
   * Adds an interceptor to change a request before it is sent to the http server.
   * This information is optional.
   *
   * @param interceptor which changes the request
   * @return the builder
   */
  ExternalTaskClientBuilder addInterceptor(ClientRequestInterceptor interceptor);

  /**
   * Specifies the amount of maximum tasks which are supposed to be fetched within one request
   * This information is optional.
   *
   * @param maxTasks which are supposed to be fetched within one request
   * @return the builder
   */
  ExternalTaskClientBuilder maxTasks(int maxTasks);

  /**
   * Specifies the maximum waiting time for the response of fetched and locked external tasks.
   * The response is performed immediately, if external tasks are available in the moment of the request.
   * This information is optional.
   *
   * @param asyncResponseTimeout of fetched and locked external tasks
   * @return the builder
   */
  ExternalTaskClientBuilder asyncResponseTimeout(long asyncResponseTimeout);

  /**
   * Bootstraps the Camunda client
   *
   * @throws ExternalTaskClientException
   * <ul>
   *   <li> if base url is null or string is empty
   *   <li> if hostname cannot be retrieved
   *   <li> if maximum amount of tasks is not greater than zero
   *   <li> if maximum asynchronous response timeout is not greater than zero
   * </ul>
   * @return the builder
   */
  ExternalTaskClient build();

}
