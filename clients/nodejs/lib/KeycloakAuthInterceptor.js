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
import {
  MISSING_KEYCLOAK_AUTH_PARAMS,
  UNEXPECTED_KEYCLOAK_TOKEN_RESULT,
} from "./__internal/errors.js";

/**
 * A KeycloakAuthInterceptor instance is an interceptor that adds a Bearer token header, containing
 * a access token, to all requests. This interceptor can be used if the Camunda REST API is protected with
 * Keycloak Gatekeeper.
 */
class KeycloakAuthInterceptor {
  /**
   * The class constructor.
   *
   * @param options The Keycloak auth interceptor options.
   * @returns function(*): {hooks: {beforeRequest: [function(*): Promise<void>]}} The interceptor function.
   * @throws Error if the required options are not set.
   */
  constructor(options) {
    if (
      !options ||
      !options.tokenEndpoint ||
      !options.clientId ||
      !options.clientSecret
    ) {
      throw new Error(MISSING_KEYCLOAK_AUTH_PARAMS);
    }

    /**
     * Bind member methods
     */
    this.getAccessToken = this.getAccessToken.bind(this);
    this.cacheToken = this.cacheToken.bind(this);
    this.interceptor = this.interceptor.bind(this);

    this.cacheOffset =
      options.cacheOffset !== undefined ? options.cacheOffset : 10;
    this.tokenEndpoint = options.tokenEndpoint;
    this.clientId = options.clientId;
    this.clientSecret = options.clientSecret;

    return this.interceptor;
  }

  /**
   * Requests a new access token from the Keycloak endpoint.
   *
   * @param tokenEndpoint The URL to the Keycloak token endpoint.
   * @param clientID      The Keycloak client ID.
   * @param clientSecret  The Keycloak client secret.
   * @returns {Promise<{access_token: String, expires_in: Number}>} The token response, containing the access token and
   * it's expiry in seconds.
   * @throws Error if an error occurred during the request.
   */
  async getAccessToken(tokenEndpoint, clientID, clientSecret) {
    const credentials = Buffer.from(`${clientID}:${clientSecret}`).toString(
      "base64"
    );

    try {
      return await got(tokenEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          Authorization: `Basic: ${credentials}`,
        },
        body: "grant_type=client_credentials",
      }).json();
    } catch (e) {
      throw new Error(
        `${UNEXPECTED_KEYCLOAK_TOKEN_RESULT} status: ${e.response.statusCode}; body: ${e.response.body}`
      );
    }
  }

  /**
   * The Keycloak access token has an expiry defined. To reduce the requests to the token endpoint, we
   * cache the token response as long it is valid.
   *
   * To poll the API always with a valid token, we subtract the `cacheOffset` from the validity. If the token is 60
   * seconds valid and we poll the API every 5 seconds, there could be the case that we poll the API exactly at the
   * time the token expires. The default `cacheOffset` from 10 seconds is a good balance between the token validity
   * and the average request duration.
   *
   * If the `expires_in` property wasn't set or if the validity is less than or equal the `cacheOffset`, we don't
   * cache the token.
   *
   * @param {{access_token: String, expires_in: Number}} token The token response.
   */
  cacheToken(token) {
    const expiresIn = token.expires_in || 0;

    if (expiresIn > this.cacheOffset) {
      this.tokenCache = token;
      const tokenCleaner = () => {
        this.tokenCache = null;
      };
      setTimeout(
        tokenCleaner.bind(this),
        (expiresIn - this.cacheOffset) * 1000
      );
    }
  }

  /**
   * The interceptor function that enriches the `got` request with the access token hook.
   *
   * @param config The `got` request config.
   * @returns {{hooks: {beforeRequest: [function(*): Promise<void>]}}} The `got` config containing the access
   * token hook.
   */
  interceptor(config) {
    // https://www.npmjs.com/package/got#hooksbeforerequest
    const hooks = {
      beforeRequest: [
        async (options) => {
          let token = this.tokenCache;
          if (!token) {
            token = await this.getAccessToken(
              this.tokenEndpoint,
              this.clientId,
              this.clientSecret
            );
            this.cacheToken(token);
          }

          if (token && token.access_token) {
            const defaultHeaders = options.headers || {};
            const headers = {
              Authorization: `Bearer ${token.access_token}`,
            };

            options.headers = { ...defaultHeaders, ...headers };
          } else {
            throw new Error(
              `${UNEXPECTED_KEYCLOAK_TOKEN_RESULT} token without access_token property: ${JSON.stringify(
                token
              )}`
            );
          }
        },
      ],
    };

    return { ...config, hooks: { ...config.hooks, ...hooks } };
  }
}

export default KeycloakAuthInterceptor;
