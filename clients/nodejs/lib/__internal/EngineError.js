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

import { RequestError } from "got";

/**
 * An error that contains the error from the Camunda REST API as also the HTTP error details like the status code
 * and status message.
 *
 * If a non 2xx status code occurs, got will throw a `got.HTTPError` that contains the error response from the engine.
 * This error isn't extendable, because it sets the error message in the constructor. Therefore we inherit from the
 * got base error type (`got.RequestError`), copy the logic form the `got.HTTPError` class and extend it with our API
 * response message.
 *
 * @see https://docs.camunda.org/manual/latest/reference/rest/overview/#error-handling
 */
class EngineError extends RequestError {
  constructor(httpError) {
    const { response, options } = httpError;

    let responseBody = null;
    try {
      responseBody = JSON.parse(response.body);
    } catch (e) {
      responseBody = response.body;
    }

    const { message, type, code } = responseBody;

    super(
      `Response code ${response.statusCode} (${
        response.statusMessage
      }); Error: ${
        message ? message : responseBody
      }; Type: ${type}; Code: ${code}`,
      {},
      options
    );
    this.name = "EngineError";

    Object.defineProperties(this, {
      response: {
        value: response,
      },
      httpStatusCode: {
        value: response.statusCode,
      },
      code: {
        value: code,
      },
      type: {
        value: type,
      },
      engineMsg: {
        value: message,
      },
    });
  }
}

export default EngineError;
