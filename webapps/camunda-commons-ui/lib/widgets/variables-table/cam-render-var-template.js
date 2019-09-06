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

var $ = require('jquery');

module.exports = [
  '$compile',
  function($compile) {
    return {
      template: '<div></div>',

      scope: {
        info: '=',
        headerName: '='
      },

      link: function($scope, element) {
        var obj = $scope.info.additions[$scope.headerName] || {};
        obj.scopeVariables = obj.scopeVariables || {};

        for (var key in obj.scopeVariables) {
          $scope[key] = obj.scopeVariables[key];
        }

        $scope.variable = $scope.info.variable;

        element.html('<div>' + obj.html + '</div>');

        $compile($('div', element)[0])($scope);
      }
    };
  }
];
