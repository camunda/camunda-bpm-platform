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

// Client
const MISSING_BASE_URL =
  "Couldn't instantiate Client, missing configuration parameter 'baseUrl'";
const WRONG_INTERCEPTOR =
  "Interceptors should be a function or an array of functions";
const WRONG_MIDDLEWARES =
  "Middleware(s) should be a function or an array of functions";
const ALREADY_REGISTERED = "Subscription failed, already subscribed to topic";
const MISSING_HANDLER = "Subscription failed, missing handler function";

// Task Service
const MISSING_TASK = "Couldn't complete task, task id is missing";
const MISSING_ERROR_CODE =
  "Couldn't throw BPMN Error, no error code was provided";
const MISSING_DURATION = "Couldn't lock task, no duration was provided";
const MISSING_NEW_DURATION =
  "Couldn't extend lock time, no new duration was provided";

// Basic Auth Interceptor
const MISSING_BASIC_AUTH_PARAMS =
  "Couldn't instantiate BasicAuthInterceptor, missing configuration parameter " +
  "'username' or 'password'";

// Keycloak Auth Interceptor
const MISSING_KEYCLOAK_AUTH_PARAMS =
  "Couldn't instantiate KeycloakAuthInterceptor, missing configuration parameter " +
  "'tokenEndpoint', 'clientId' or 'clientSecret'";
const UNEXPECTED_KEYCLOAK_TOKEN_RESULT =
  "Couldn't get access token from Keycloak provider; got";

// FileService
const MISSING_FILE_OPTIONS =
  "Couldn't create a File, make sure to provide one of the following" +
  " parameters: \n- path \ntypedValue";

const WRONG_SORTING =
  "Couldn't instantiate Client, 'sorting' parameter should be an array.";
const WRONG_SORTING_SORT_BY =
  "Couldn't instantiate Client, wrong 'sorting.sortBy' parameter. Possible values: ";
const WRONG_SORTING_SORT_ORDER =
  "Couldn't instantiate Client, wrong 'sorting.sortOrder' parameter. Possible values: ";

export {
  MISSING_BASE_URL,
  ALREADY_REGISTERED,
  MISSING_HANDLER,
  MISSING_TASK,
  WRONG_INTERCEPTOR,
  MISSING_ERROR_CODE,
  MISSING_DURATION,
  MISSING_NEW_DURATION,
  MISSING_BASIC_AUTH_PARAMS,
  MISSING_KEYCLOAK_AUTH_PARAMS,
  UNEXPECTED_KEYCLOAK_TOKEN_RESULT,
  WRONG_MIDDLEWARES,
  MISSING_FILE_OPTIONS,
  WRONG_SORTING,
  WRONG_SORTING_SORT_BY,
  WRONG_SORTING_SORT_ORDER,
};
