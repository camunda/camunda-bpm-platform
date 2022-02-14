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

'use strict';

/**
 * A set of utilities for routing
 * @exports routeUtil
 */
var utils = {};

/**
 * Determine where (and if) the user should be redirected
 * when landing on a the `process-definition` or `process-instance`
 * base URL.
 * {@link http://code.angularjs.org/1.1.5/docs/api/ng.$routeProvider|see ng.$routeProvider documentation}
 *
 * @param {Object} params         - The route parameters as used in
 *                                  a angular route
 * @param {string} currentPath    - The actual path of the request,
 *                                  generally something like:
 *                                  `/process-definition/<processDefinitionId>`
 * @param {Object} currentSearch  - The parsed object of the route "search" part
 * @return {string}               - A URL string to be redirected to
 */
utils.redirectToRuntime = function(params, currentPath, currentSearch) {
  var redirectUrl = currentPath + '/runtime';

  return utils.redirectTo(redirectUrl, currentSearch, true);
};

/**
 * Replace the last fragment of a given path with a given replacement.
 *
 * @param {string} replacement    - The replacement.
 * @param {string} currentPath    - The actual path of the request,
 *                                  generally something like:
 *                                  `/process-definition/<processDefinitionId>/runtime`
 * @param {Object} currentSearch  - The parsed object of the route "search" part
 * @return {string}               - A URL string to be redirected to
 */
utils.replaceLastPathFragment = function(
  replacement,
  currentPath,
  currentSearch,
  keepSearchParams
) {
  var redirectUrl = currentPath.replace(/[^/]*$/, replacement);

  return utils.redirectTo(redirectUrl, currentSearch, keepSearchParams);
};

utils.redirectTo = function(redirectUrl, currentSearch, keepSearchParams) {
  var search = [],
    key;

  if (currentSearch && keepSearchParams) {
    var isArray =
      Object.prototype.toString.call(keepSearchParams) === '[object Array]';

    for (key in currentSearch) {
      if (isArray) {
        if (keepSearchParams.indexOf(key) === -1) {
          continue;
        }
      }

      search.push(key + '=' + encodeURIComponent(currentSearch[key]));
    }
  }

  return redirectUrl + (search.length ? '?' + search.join('&') : '');
};

module.exports = utils;
