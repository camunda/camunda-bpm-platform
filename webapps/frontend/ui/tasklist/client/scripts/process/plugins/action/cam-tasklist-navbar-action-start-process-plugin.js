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

var angular = require('camunda-commons-ui/vendor/angular');

var startProcessActionTemplate = require('./cam-tasklist-navbar-action-start-process-plugin.html?raw');
var template = require('./modals/cam-tasklist-process-start-modal.html?raw');

var Controller = [
  '$scope',
  '$uibModal',
  '$q',
  'camAPI',
  'dataDepend',
  '$location',
  'search',
  function($scope, $modal, $q, camAPI, dataDepend, $location, search) {
    var ProcessDefinition = camAPI.resource('process-definition');

    var processData = ($scope.processData = dataDepend.create($scope));

    var DEFAULT_PROCESS_DEFINITION_QUERY = {
      latest: true,
      active: true,
      startableInTasklist: true,
      startablePermissionCheck: true,
      firstResult: 0,
      maxResults: 15
    };

    processData.provide(
      'processDefinitionQuery',
      DEFAULT_PROCESS_DEFINITION_QUERY
    );

    processData.provide('processDefinitions', [
      'processDefinitionQuery',
      function(processDefinitionQuery) {
        var deferred = $q.defer();

        ProcessDefinition.list(processDefinitionQuery, function(err, res) {
          if (err) {
            deferred.reject(err);
          } else {
            deferred.resolve(res);
          }
        });

        return deferred.promise;
      }
    ]);

    processData.provide('currentProcessDefinitionId', {id: null});

    processData.provide('startForm', [
      'currentProcessDefinitionId',
      function(currentProcessDefinitionId) {
        var deferred = $q.defer();

        if (!currentProcessDefinitionId.id) {
          deferred.resolve(null);
        } else {
          ProcessDefinition.startForm(currentProcessDefinitionId, function(
            err,
            res
          ) {
            if (err) {
              deferred.reject(err);
            } else {
              deferred.resolve(res);
            }
          });
        }

        return deferred.promise;
      }
    ]);

    var modalResolved = true;

    $scope.open = function(ignoreSearch) {
      if (!modalResolved) {
        return;
      }

      if (!ignoreSearch) {
        search.updateSilently({processStart: true});
      }

      processData.set(
        'processDefinitionQuery',
        angular.copy(DEFAULT_PROCESS_DEFINITION_QUERY)
      );
      modalResolved = false;
      var modalInstance = $modal.open({
        size: 'lg',
        controller: 'camProcessStartModalCtrl',
        template: template,
        resolve: {
          processData: function() {
            return processData;
          }
        }
      });

      modalInstance.result.then(
        function() {
          search.updateSilently({processStart: null, processTenant: null});
          modalResolved = true;
          $scope.$root.$broadcast('refresh');
          document.querySelector('.start-process-action a').focus();
        },
        function() {
          search.updateSilently({processStart: null, processTenant: null});
          modalResolved = true;
          document.querySelector('.start-process-action a').focus();
        }
      );
    };

    //open if deep-linked
    var openFromUri = function() {
      if (modalResolved && $location.search()['processStart']) {
        $scope.open(true);
      }
    };

    $scope.$on('$locationChangeSuccess', openFromUri);
    $scope.$on('shortcut:startProcess', $scope.open);

    openFromUri();
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'start-process-action',
    template: startProcessActionTemplate,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
