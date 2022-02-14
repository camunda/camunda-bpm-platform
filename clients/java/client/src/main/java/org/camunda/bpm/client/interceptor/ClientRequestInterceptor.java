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
package org.camunda.bpm.client.interceptor;

/**
 * <p>Client request interceptor makes it possible to apply changes to each request before it is sent to the http server</p>
 *
 * @author Tassilo Weidner
 */
@FunctionalInterface
public interface ClientRequestInterceptor {

  /**
   * Has been invoked before a request is sent to the http server
   *
   * @param requestContext provides the data of the request and offers methods to change it
   */
  void intercept(ClientRequestContext requestContext);

}

