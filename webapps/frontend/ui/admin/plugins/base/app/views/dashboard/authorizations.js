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

var template = require('./authorizations.html?raw');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('admin.dashboard.section', {
      id: 'authorization',
      label: 'AUTHORIZATION_AUTHORIZATIONS',
      template: template,
      pagePath: '#/authorization',
      controller: [function() {}],
      access: [
        'AuthorizationResource',
        function(AuthorizationResource) {
          return function(cb) {
            AuthorizationResource.check({
              permissionName: 'ALL',
              resourceName: 'authorization',
              resourceType: 4
            })
              .$promise.then(function(response) {
                cb(null, response.authorized);
              })
              .catch(cb);
          };
        }
      ],
      priority: 0
    });
  }
];
