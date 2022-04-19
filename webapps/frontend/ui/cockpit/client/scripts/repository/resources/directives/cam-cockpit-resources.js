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

var template = require('./cam-cockpit-resources.html')();

var angular = require('../../../../../../../camunda-commons-ui/vendor/angular');

var $ = angular.element;

module.exports = [
  function() {
    return {
      restrict: 'A',
      scope: {
        repositoryData: '='
      },

      template: template,

      controller: [
        '$scope',
        '$location',
        '$timeout',
        'search',
        function($scope, $location, $timeout, search) {
          var resourcesData = $scope.repositoryData.newChild($scope);

          // utilities /////////////////////////////////////////////////////////////////

          var updateSilently = function(params) {
            search.updateSilently(params);
          };

          // observe data //////////////////////////////////////////////////////////////

          $scope.state = resourcesData.observe('resourceId', function(
            resourceId
          ) {
            $scope.currentResourceId = resourceId.resourceId;
          });

          // taken from
          // http://stackoverflow.com/questions/1916218/find-the-longest-common-starting-substring-in-a-set-of-strings#answer-1917041
          function sharedStart(array) {
            if (!array.length) {
              // fix for empty array
              return '';
            }

            var A = array.concat().sort(),
              a1 = A[0],
              a2 = A[A.length - 1],
              L = a1.length,
              i = 0;
            while (i < L && a1.charAt(i) === a2.charAt(i)) i++;
            return a1.substring(0, i);
          }

          $scope.sharedStart = '';
          $scope.state = resourcesData.observe('resources', function(
            resources
          ) {
            var paths = [];
            $scope.resources = (resources || []).map(function(resource) {
              var parts = (resource.name || resource.id).split('/');
              resource._filename = parts.pop();
              resource._filepath = parts.join('/');
              paths.push(resource._filepath);
              return resource;
            });
            $scope.sharedStart = sharedStart(paths);
          });

          $scope.truncateFilepath = function(filepath) {
            return filepath.slice($scope.sharedStart.length);
          };

          // selection //////////////////////////////////////////////////////////////////

          $scope.focus = function($event, resource) {
            if ($event) {
              $event.preventDefault();
            }

            var resourceId = resource.id;

            if ($scope.currentResourceId === resourceId) {
              updateSilently({
                resource: resourceId,
                resourceName: null
              });
            } else {
              updateSilently({
                resource: resourceId,
                resourceName: null,
                viewbox: null
              });
            }

            resourcesData.changed('resourceId');
          };

          var selectNextResource = function() {
            for (var i = 0; i < $scope.resources.length - 1; i++) {
              if ($scope.resources[i].id === $scope.currentResourceId) {
                return $scope.focus(null, $scope.resources[i + 1]);
              }
            }
          };

          var selectPreviousResource = function() {
            for (var i = 1; i < $scope.resources.length; i++) {
              if ($scope.resources[i].id === $scope.currentResourceId) {
                return $scope.focus(null, $scope.resources[i - 1]);
              }
            }
          };

          $scope.handleKeydown = function($event) {
            if ($event.keyCode === 40) {
              $event.preventDefault();
              selectNextResource($event);
            } else if ($event.keyCode === 38) {
              $event.preventDefault();
              selectPreviousResource();
            }
            // wait for angular to update the classes and scroll to the newly selected task
            $timeout(function() {
              var $el = $($event.target).find('li.active')[0];
              if ($el) {
                $el.scrollIntoView(false);
              }
            });
          };
        }
      ]
    };
  }
];
