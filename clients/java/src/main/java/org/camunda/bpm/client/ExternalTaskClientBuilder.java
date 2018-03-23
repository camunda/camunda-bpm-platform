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
   * @param baseUrl of the Camunda BPM Platform REST API
   * @return the builder
   */
  ExternalTaskClientBuilder baseUrl(String baseUrl);

  /**
   * Adds an interceptor to change a request before it is sent to the http server
   *
   * @param interceptor which changes the request
   * @return the builder
   */
  ExternalTaskClientBuilder addInterceptor(ClientRequestInterceptor interceptor);

  /**
   * Bootstraps the Camunda client
   *
   * @throws ExternalTaskClientException
   * <ul>
   *   <li> if base url is null or string is empty
   *   <li> if hostname cannot be retrieved
   * </ul>
   * @return the builder
   */
  ExternalTaskClient build();

}
