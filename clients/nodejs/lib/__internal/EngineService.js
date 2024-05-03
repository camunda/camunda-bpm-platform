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

import { got, HTTPError } from "got";
import EngineError from "./EngineError.js";

class EngineService {
  constructor({ workerId, baseUrl, interceptors }) {
    this.workerId = workerId;
    this.baseUrl = `${baseUrl.replace(/\/$/, "")}`;
    this.interceptors = interceptors;

    /**
     * Bind member methods
     */
    this.request = this.request.bind(this);
    this.post = this.post.bind(this);
    this.get = this.get.bind(this);
    this.fetchAndLock = this.fetchAndLock.bind(this);
    this.complete = this.complete.bind(this);
    this.handleFailure = this.handleFailure.bind(this);
    this.lock = this.lock.bind(this);
  }

  async request(method, path, options) {
    const url = `${this.baseUrl}${path}`;
    let newOptions = { method, ...options };

    if (this.interceptors) {
      newOptions = this.interceptors.reduce((config, interceptor) => {
        return interceptor(config);
      }, newOptions);
    }

    try {
      const { body, headers } = await got(url, {
        ...newOptions,
        responseType: "buffer",
      });
      if (headers["content-type"] === "application/json") {
        return JSON.parse(body.toString("utf-8"));
      } else {
        return body;
      }
    } catch (e) {
      if (e instanceof HTTPError) {
        throw new EngineError(e);
      }

      throw e;
    }
  }

  /**
   * @throws HTTPError
   * @param path
   * @param options
   * @returns {Promise}
   */
  post(path, options) {
    return this.request("POST", path, options);
  }

  /**
   * @throws HTTPError
   * @param path
   * @param options
   * @returns {Promise}
   */
  get(path, options) {
    return this.request("GET", path, options);
  }

  /**
   * @throws HTTPError
   * @param requestBody
   * @returns {Promise}
   */
  fetchAndLock(requestBody) {
    return this.post("/external-task/fetchAndLock", {
      json: { ...requestBody, workerId: this.workerId },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @param variables
   * @param localVariables
   * @returns {Promise}
   */
  complete({ id, variables, localVariables }) {
    return this.post(`/external-task/${id}/complete`, {
      json: { workerId: this.workerId, variables, localVariables },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @param options
   * @returns {Promise}
   */
  handleFailure({ id }, options) {
    return this.post(`/external-task/${id}/failure`, {
      json: { ...options, workerId: this.workerId },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @param errorCode
   * @param errorMessage
   * @param variables
   * @returns {Promise}
   */
  handleBpmnError({ id }, errorCode, errorMessage, variables) {
    return this.post(`/external-task/${id}/bpmnError`, {
      json: { errorCode, workerId: this.workerId, errorMessage, variables },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @param lockDuration
   * @returns {Promise}
   */
  lock({ id }, lockDuration) {
    return this.post(`/external-task/${id}/lock`, {
      json: {
        lockDuration,
        workerId: this.workerId,
      },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @param newDuration
   * @returns {Promise}
   */
  extendLock({ id }, newDuration) {
    return this.post(`/external-task/${id}/extendLock`, {
      json: { newDuration, workerId: this.workerId },
    });
  }

  /**
   * @throws HTTPError
   * @param id
   * @returns {Promise}
   */
  unlock({ id }) {
    return this.post(`/external-task/${id}/unlock`, {});
  }
}

export default EngineService;
