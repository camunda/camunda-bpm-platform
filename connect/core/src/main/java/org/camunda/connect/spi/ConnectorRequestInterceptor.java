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
 * <p>A request interceptor. An interceptor allows intercepting the request created by a connector.
 * See {@link #handleInvocation(ConnectorInvocation)} for more details and usage patters.</p>
 *
 * <p>The interceptor is invoked for every request created by a connector. As such, an
 * interceptor implementation must be thread-safe.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ConnectorRequestInterceptor {

  /**
   * <p>Intercept and handle the actual invocation. An implementation of this method *must*
   * call <code>invocation.proceed();</code> and return the result:</p>
   *
   * <pre>
   *  public Object handleInvocation(ConnectorInvocation invocation) throws Exception {
   *    // do something before the request
   *    try {
   *      Object result = invocation.proceed();
   *      // do something after a successful request
   *      return result;
   *    } catch(Exception e) {
   *      // do something after a failed request
   *      throw e;
   *    }
   *  }
   * </pre>
   *
   * <p>This may be useful for "management" use cases such as logging, failure tracking etc... and
   * modifying the Thread context of the request thread (Security Context, Transactions, ...).</p>
   *
   * <p>It is also possible for an implementation to access and modify the low level "raw" request
   * object before it is executed by the connector:</p>
   * <pre>
   *  public Object handleInvocation(ConnectorInvocation invocation) throws Exception {
   *    Object rawRequest = invocation.getTarget();
   *    // cast rawRequest to the low-level connector implementation object and work with it.
   *
   *    // finally
   *    return invocation.proceed();
   *  }
   * </pre>
   *
   * @param invocation the invocation
   * @return the result of the invocation
   * @throws Exception
   */
  Object handleInvocation(ConnectorInvocation invocation) throws Exception;

}
