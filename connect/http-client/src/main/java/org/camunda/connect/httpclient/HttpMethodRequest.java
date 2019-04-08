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
package org.camunda.connect.httpclient;

public interface HttpMethodRequest<Q extends HttpMethodRequest<?, ?>, R extends HttpResponse> extends HttpBaseRequest<Q, R> {

  /**
   * Set GET as request method
   *
   * @return this request
   */
  Q get();

  /**
   * Set POST as request method
   *
   * @return this request
   */
  Q post();

  /**
   * Set PUT as request method
   *
   * @return this request
   */
  Q put();

  /**
   * Set DELETE as request method
   *
   * @return this request
   */
  Q delete();

  /**
   * Set PATCH as request method
   *
   * @return this request
   */
  Q patch();

  /**
   * Set HEAD as request method
   *
   * @return this request
   */
  Q head();

  /**
   * Set OPTIONS as request method
   *
   * @return this request
   */
  Q options();

  /**
   * Set TRACE as request method
   *
   * @return this request
   */
  Q trace();

}
