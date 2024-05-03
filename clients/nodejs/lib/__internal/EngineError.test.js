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

import got from "got";
import EngineError from "./EngineError.js";

describe("EngineError", () => {
  test("construct an error from a Camunda REST API error", () => {
    // given
    const response = {
      body: {
        type: "SomeExceptionClass",
        message: "a detailed message",
        code: 33333,
      },
      statusCode: 400,
      statusMessage: "Bad request",
    };
    const httpError = new got.HTTPError(response);
    httpError.response = response;
    const expectedPayload =
      "Response code 400 (Bad request); Error: a detailed message; Type: SomeExceptionClass; Code: 33333";

    // when
    const engineError = new EngineError(httpError);

    // then
    expect(engineError.message).toEqual(expectedPayload);
    expect(engineError.engineMsg).toEqual("a detailed message");
    expect(engineError.code).toEqual(33333);
    expect(engineError.type).toEqual("SomeExceptionClass");
    expect(engineError.httpStatusCode).toEqual(400);
  });

  test("construct an error with an unexpected response body", () => {
    // given
    const response = {
      body: "Some unexpected error message",
      statusCode: 400,
      statusMessage: "Bad request",
    };
    const httpError = new got.HTTPError(response);
    httpError.response = response;
    const expectedPayload =
      "Response code 400 (Bad request); Error: Some unexpected error message; Type: undefined; Code: undefined";

    // when
    const engineError = new EngineError(httpError);

    // then
    expect(engineError.httpStatusCode).toEqual(400);
    expect(engineError.message).toEqual(expectedPayload);
    expect(engineError.code).toBeUndefined();
    expect(engineError.type).toBeUndefined();
  });
});
