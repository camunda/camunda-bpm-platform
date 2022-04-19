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

var fs = require('fs');

var template = require('./cam-cockpit-resource-meta.html')();

module.exports = [
  function() {
    return {
      scope: {
        resourceData: '=',
        control: '='
      },

      template: template,

      controller: [
        '$scope',
        function($scope) {
          // fields ////////////////////////////////////////////////////

          var resourceMetaData = $scope.resourceData.newChild($scope);
          $scope.isDmnResource = $scope.control.isDmnResource;

          // observe //////////////////////////////////////////////////

          resourceMetaData.observe('resource', function(resource) {
            if (resource) {
              var parts = (resource.name || resource.id).split('/');
              resource._filename = parts.pop();
              resource._filepath = parts.join('/');
            }

            $scope.resource = resource;
          });

          resourceMetaData.observe('definitions', function(definitions) {
            $scope.definitions = definitions;
          });
        }
      ]
    };
  }
];
