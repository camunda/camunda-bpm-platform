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

import BasicAuthInterceptor from "./BasicAuthInterceptor.js";
import { MISSING_BASIC_AUTH_PARAMS } from "./__internal/errors.js";

describe("BasicAuthInterceptor", () => {
  test("should throw error if username or password are missing", () => {
    expect(() => new BasicAuthInterceptor()).toThrowError(
      MISSING_BASIC_AUTH_PARAMS
    );
    expect(
      () => new BasicAuthInterceptor({ username: "some username" })
    ).toThrowError(MISSING_BASIC_AUTH_PARAMS);
    expect(
      () => new BasicAuthInterceptor({ password: "some password" })
    ).toThrowError(MISSING_BASIC_AUTH_PARAMS);
  });

  test("should add basic auth header to intercepted config", () => {
    // given
    const basicAuthInterceptor = new BasicAuthInterceptor({
      username: "some username",
      password: "some password",
    });
    const config = { key: "some value" };
    const headers = basicAuthInterceptor(config);

    // then
    expect(headers).toMatchSnapshot();
  });
});
