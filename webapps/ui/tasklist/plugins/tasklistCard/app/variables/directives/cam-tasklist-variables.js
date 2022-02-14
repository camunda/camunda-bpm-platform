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

var template = fs.readFileSync(
  __dirname + '/cam-tasklist-variables.html',
  'utf8'
);
var modalTemplate = fs.readFileSync(
  __dirname + '/../modals/cam-tasklist-variables-detail-modal.html',
  'utf8'
);

var angular = require('../../../../../../../camunda-commons-ui/vendor/angular');

// AngularJS DI
module.exports = [
  '$uibModal',
  '$window',
  'Uri',
  function($modal, $window, Uri) {
    return {
      template: template,

      scope: {
        variables: '=',
        filterProperties: '='
      },

      link: function(scope) {
        scope.variableDefinitions = [];
        scope.variablesByName = {};
        scope.expanded = false;
        scope.shownVariablesCount = 0;

        scope.showValue = function(variable, $event) {
          $event.preventDefault();
          $event.stopPropagation();
          $modal
            .open({
              template: modalTemplate,

              windowClass: 'variable-modal-detail',

              resolve: {
                details: function() {
                  return variable;
                }
              },

              controller: 'camTasklistVariablesDetailsModalCtrl'
            })
            .result.catch(angular.noop);
        };

        scope.download = function(variable, $event) {
          $event.preventDefault();
          $event.stopPropagation();
          var link = variable._links.self.href + '/data';
          link = Uri.appUri('engine://engine/:engine' + link);
          $window.open(link, 'download');
        };

        if (scope.filterProperties) {
          scope.variableDefinitions = scope.filterProperties.variables || {};

          // building an object on which keys are name of variables is more efficient
          // than calling a function which would iterate every time.
          angular.forEach(scope.variables, function(variable) {
            scope.variablesByName[variable.name] = variable;
          });

          scope.shownVariablesCount = Object.keys(
            scope.filterProperties.showUndefinedVariable
              ? scope.variableDefinitions
              : scope.variablesByName
          ).length;
        }
      }
    };
  }
];
