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
import got from "got";
import KeycloakAuthInterceptor from "./KeycloakAuthInterceptor.js";
import {
  MISSING_KEYCLOAK_AUTH_PARAMS,
  UNEXPECTED_KEYCLOAK_TOKEN_RESULT,
} from "./__internal/errors.js";

describe("KeycloakAuthInterceptor", () => {
  afterEach(() => {
    got.mockClear();
  });

  test("should throw error if tokenEndpoint, clientId or clientSecret are missing", () => {
    expect(() => new KeycloakAuthInterceptor()).toThrowError(
      MISSING_KEYCLOAK_AUTH_PARAMS
    );
    expect(
      () => new KeycloakAuthInterceptor({ tokenEndpoint: "some endpoint" })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
    expect(
      () => new KeycloakAuthInterceptor({ clientId: "some id" })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
    expect(
      () => new KeycloakAuthInterceptor({ clientSecret: "some secret" })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
    expect(
      () =>
        new KeycloakAuthInterceptor({
          tokenEndpoint: "some endpoint",
          clientId: "some id",
        })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
    expect(
      () =>
        new KeycloakAuthInterceptor({
          tokenEndpoint: "some endpoint",
          clientSecret: "some secret",
        })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
    expect(
      () =>
        new KeycloakAuthInterceptor({
          clientId: "some password",
          clientSecret: "some secret",
        })
    ).toThrowError(MISSING_KEYCLOAK_AUTH_PARAMS);
  });

  test("should throw error if token endpoint returns unexpected HTTP response", async () => {
    // given
    const response = {
      body: "Some keycloak error",
      statusCode: 400,
      statusMessage: "Bad request",
    };
    const error = new got.HTTPError(response);
    error.response = response;
    got.mockReturnValue({ json: () => Promise.reject(error) });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
    });

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];

    // then
    try {
      await hook({});
    } catch (e) {
      expect(e).toEqual(
        new Error(
          `${UNEXPECTED_KEYCLOAK_TOKEN_RESULT} status: 400; body: Some keycloak error`
        )
      );
    }
  });

  test("should throw error if token doesn't contains the access token", async () => {
    // given
    got.mockReturnValue({ json: () => Promise.resolve({}) });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
    });

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];

    // then
    try {
      await hook({});
    } catch (e) {
      expect(e).toEqual(
        new Error(
          `${UNEXPECTED_KEYCLOAK_TOKEN_RESULT} token without access_token property: {}`
        )
      );
    }
  });

  test("should add auth token to intercepted config", async () => {
    // given
    got.mockReturnValue({
      json: () => Promise.resolve({ access_token: "1234567890" }),
    });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
    });
    const options = { key: "some value" };

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];
    await hook(options);

    // then
    expect(options).toMatchSnapshot();
  });

  test("should cache the token response if token expiry is greater than cacheOffset", async () => {
    // given
    const tokenResponse = { access_token: "1234567890", expires_in: 5 };
    got.mockReturnValue({ json: () => Promise.resolve(tokenResponse) });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
      cacheOffset: 0,
    });

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];
    await hook({});
    await hook({});

    // then
    expect(got).toHaveBeenCalledTimes(1);
  });

  test("should not cache the token response if token expiry is less than cacheOffset", async () => {
    // given
    const tokenResponse = { access_token: "1234567890", expires_in: 5 };
    got.mockReturnValue({ json: () => Promise.resolve(tokenResponse) });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
      cacheOffset: 10,
    });

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];
    await hook({});
    await hook({});

    // then
    expect(got).toHaveBeenCalledTimes(2);
  });

  test("should clear the token cache", async () => {
    jest.useFakeTimers();

    // given
    const tokenResponse = { access_token: "1234567890", expires_in: 5 };
    got.mockReturnValue({ json: () => Promise.resolve(tokenResponse) });
    const keycloakAuthInterceptor = new KeycloakAuthInterceptor({
      tokenEndpoint: "some endpoint",
      clientId: "some id",
      clientSecret: "some secret",
      cacheOffset: 0,
    });

    // when
    const { hooks } = keycloakAuthInterceptor({});
    const hook = hooks.beforeRequest[0];
    await hook({});
    jest.runAllTimers();
    await hook({});

    // then
    expect(got).toHaveBeenCalledTimes(2);

    jest.useRealTimers();
  });
});
