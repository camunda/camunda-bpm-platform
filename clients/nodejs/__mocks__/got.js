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

import { jest } from "@jest/globals";
// There is no alternative of `requireActual` for esm as of now
// See `jest.importActual` progress in https://github.com/facebook/jest/pull/10976.
// const got = jest.requireActual("got");
// const got = jest.createMockFromModule("got");

const handleRequest = (url, { testResult }) => {
  if (testResult instanceof Error) {
    return Promise.reject(testResult);
  }
  return {
    body:
      testResult instanceof Object
        ? JSON.stringify(testResult)
        : Buffer.from(testResult || "", "utf-8"),
    headers: {
      "content-type":
        testResult instanceof Object
          ? "application/json"
          : "application/octet-stream",
    },
    // headers: {},
  };
};

const gotMock = handleRequest;
gotMock.json = () => {};

const myModule = jest.fn().mockImplementation(gotMock);

class HTTPError extends Error {}
class RequestError extends Error {}
myModule.HTTPError = HTTPError;
myModule.RequestError = RequestError;

export default myModule;
export { myModule as got, HTTPError, RequestError };
