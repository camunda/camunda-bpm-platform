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

import { MISSING_BASIC_AUTH_PARAMS } from "./__internal/errors.js";

class BasicAuthInterceptor {
  /**
   * @throws Error
   */
  constructor(options) {
    if (!options || !options.username || !options.password) {
      throw new Error(MISSING_BASIC_AUTH_PARAMS);
    }

    /**
     * Bind member methods
     */
    this.getHeader = this.getHeader.bind(this);
    this.interceptor = this.interceptor.bind(this);

    this.header = this.getHeader(options);

    return this.interceptor;
  }

  getHeader({ username, password }) {
    const encoded = Buffer.from(`${username}:${password}`).toString("base64");
    return { Authorization: `Basic ${encoded}` };
  }

  interceptor(config) {
    return { ...config, headers: { ...config.headers, ...this.header } };
  }
}

export default BasicAuthInterceptor;
