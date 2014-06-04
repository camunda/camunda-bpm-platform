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
package org.camunda.bpm.connect.impl;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.connect.Connector;
import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.ConnectorResponse;
import org.camunda.bpm.connect.interceptor.RequestInterceptor;

/**
 * Abstract implementation of the connector interface.
 *
 * This implementation provides a linked list of interceptors and related methods for
 * handling interceptor invocation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractConnector<T extends ConnectorRequest<?>, R extends ConnectorResponse> implements Connector<T> {

  /**
   * The {@link RequestInterceptor} chain
   */
  protected List<RequestInterceptor> requestInterceptors = new LinkedList<RequestInterceptor>();

  public List<RequestInterceptor> getRequestInterceptors() {
    return requestInterceptors;
  }

  public void addRequestInterceptor(RequestInterceptor interceptor) {
    requestInterceptors.add(interceptor);
  }

  /**
   * Execute the request on the connector.
   * @param request the request
   * @return the result.
   */
  public abstract R execute(T request);

}
