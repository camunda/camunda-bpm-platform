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

module.exports = [
  '$location',
  'routeUtil',
  function($location, routeUtil) {
    return function(deployment, resource) {
      var path = '#/repository';

      var searches = {
        deployment: deployment.id,
        deploymentsQuery: JSON.stringify([
          {
            type: 'id',
            operator: 'eq',
            value: deployment.id
          }
        ])
      };

      if (resource) {
        searches.resourceName = resource.name;
      }

      var searchParams = $location.search() || {};
      if (searchParams['deploymentsSortBy']) {
        searches['deploymentsSortBy'] = searchParams['deploymentsSortBy'];
      }

      if (searchParams['deploymentsSortOrder']) {
        searches['deploymentsSortOrder'] = searchParams['deploymentsSortOrder'];
      }

      return routeUtil.redirectTo(path, searches, [
        'deployment',
        'resourceName',
        'deploymentsQuery',
        'deploymentsSortOrder',
        'deploymentsSortBy'
      ]);
    };
  }
];
