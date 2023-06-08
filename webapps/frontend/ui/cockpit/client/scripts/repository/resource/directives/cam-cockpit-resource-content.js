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

var template = require('./cam-cockpit-resource-content.html?raw');

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
        'configuration',
        function($scope, configuration) {
          $scope.bpmnJsConf = configuration.getBpmnJs();

          // fields ////////////////////////////////////////////////////

          var resourceContentData = $scope.resourceData.newChild($scope);

          var resource;

          $scope.isBpmnResource = $scope.control.isBpmnResource;
          $scope.isCmmnResource = $scope.control.isCmmnResource;
          $scope.isDmnResource = $scope.control.isDmnResource;
          $scope.isImageResource = $scope.control.isImageResource;
          $scope.isHtmlResource = $scope.control.isHtmlResource;
          $scope.isFormResource = $scope.control.isFormResource;
          $scope.isUnkownResource = $scope.control.isUnkownResource;
          $scope.imageLink = $scope.control.downloadLink;

          // observe //////////////////////////////////////////////////

          resourceContentData.observe('resource', function(_resource) {
            if (_resource && resource && _resource.id !== resource.id) {
              $scope.binary = null;
            }
            resource = $scope.resource = _resource;
          });

          resourceContentData.observe('currentDeployment', function(
            _deployment
          ) {
            $scope.deployment = _deployment;
          });

          resourceContentData.observe('binary', function(binary) {
            $scope.binary = (binary || {}).data;
          });
        }
      ]
    };
  }
];
