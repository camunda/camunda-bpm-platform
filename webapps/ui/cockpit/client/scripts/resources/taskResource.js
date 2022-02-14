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
var TaskResource = [
  '$resource',
  'Uri',
  function($resource, Uri) {
    var endpoint = Uri.appUri(
      'engine://engine/:engine/task/:id/:action/:subAction'
    );
    var endpointParams = {id: '@id'};

    return $resource(endpoint, endpointParams, {
      query: {
        method: 'POST',
        isArray: true
      },
      count: {
        method: 'POST',
        isArray: false,
        params: {id: 'count'}
      },

      getIdentityLinks: {
        method: 'GET',
        isArray: true,
        params: {action: 'identity-links'}
      },
      addIdentityLink: {
        method: 'POST',
        params: {action: 'identity-links'}
      },
      deleteIdentityLink: {
        method: 'POST',
        params: {
          action: 'identity-links',
          subAction: 'delete'
        }
      },

      setAssignee: {
        method: 'POST',
        params: {action: 'assignee'}
      }
    });
  }
];

module.exports = TaskResource;
