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

module.exports = [
  '$scope',
  '$q',
  '$location',
  '$timeout',
  '$rootScope',
  'search',
  'dataDepend',
  'page',
  'camAPI',
  '$translate',
  function(
    $scope,
    $q,
    $location,
    $timeout,
    $rootScope,
    search,
    dataDepend,
    page,
    camAPI,
    $translate
  ) {
    var Deployment = camAPI.resource('deployment');
    $scope.$root.showBreadcrumbs = false;

    $scope.control = {
      deployments: []
    };

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      label: $translate.instant('REPOSITORY_CONTROLLER_CAM_BREAD_CRUMB')
    });

    page.titleSet($translate.instant('REPOSITORY_CONTROLLER_CAM_TITLE_SET'));

    // utilities /////////////////////////////////////////////////////////////////
    var updateSilently = function(params) {
      search.updateSilently(params);
    };

    var getPropertyFromLocation = function(property) {
      var search = $location.search() || {};
      return search[property] || null;
    };
    // fields ///////////////////////////////////////////////////

    // init data depend for deployments data
    var repositoryData = ($scope.repositoryData = dataDepend.create($scope));

    var deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
    var deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
    var deploymentId = getPropertyFromLocation('deployment');
    var resourceId = getPropertyFromLocation('resource');
    var resourceName = getPropertyFromLocation('resourceName');

    // provide data //////////////////////////////////////////////////////////////////
    repositoryData.provide('deploymentsSorting', function() {
      deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
      deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
      return {
        sortBy: deploymentsSortBy || 'deploymentTime',
        sortOrder: deploymentsSortOrder || 'desc'
      };
    });

    repositoryData.provide('deployments', []);

    $scope.$watch('control.deployments', function(deployments) {
      repositoryData.set('deployments', deployments);
    });

    repositoryData.provide('currentDeployment', [
      'deployments',
      function(deployments) {
        deployments = deployments || [];

        var focused;
        var _deploymentId = getPropertyFromLocation('deployment');

        for (var i = 0, deployment; (deployment = deployments[i]); i++) {
          if (_deploymentId === deployment.id) {
            focused = deployment;
            break;
          }
          // auto focus first deployment
          if (!focused) {
            focused = deployment;
          }
        }

        if (focused) {
          deploymentId = focused.id;

          if (focused.id !== _deploymentId) {
            updateSilently({
              deployment: focused.id,
              resource: null,
              viewbox: null
            });
            $location.replace();
          }
        } else {
          updateSilently({
            deployment: null,
            resource: null,
            viewbox: null
          });
          $location.replace();
        }

        return angular.copy(focused);
      }
    ]);

    repositoryData.provide('resources', [
      'currentDeployment',
      function(currentDeployment) {
        var deferred = $q.defer();

        if (!currentDeployment || currentDeployment.id === null) {
          deferred.resolve(null);
        } else {
          Deployment.getResources(currentDeployment.id, function(err, res) {
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

    repositoryData.provide('resourceId', [
      'resources',
      function(resources) {
        resourceId = getPropertyFromLocation('resource');
        resourceName = getPropertyFromLocation('resourceName');

        if (resourceId) {
          return {
            resourceId: resourceId
          };
        } else if (resourceName) {
          resources = resources || [];
          for (var i = 0, resource; (resource = resources[i]); i++) {
            if (resource.name === resourceName) {
              return {
                resourceId: resource.id
              };
            }
          }
        }

        return {
          resourceId: null
        };
      }
    ]);

    repositoryData.provide('resource', [
      'resourceId',
      'currentDeployment',
      function(resourceId, deployment) {
        var deferred = $q.defer();

        resourceId = resourceId.resourceId;

        if (typeof resourceId !== 'string') {
          deferred.resolve(null);
        } else if (!deployment || deployment.id === null) {
          deferred.resolve(null);
        } else {
          Deployment.getResource(deployment.id, resourceId, function(err, res) {
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

    $scope.onDeployed = function() {
      $rootScope.$broadcast('cam-common:cam-searchable:query-force-change');
    };

    $scope.$on('$routeChanged', function() {
      var oldDeploymentsSortBy = deploymentsSortBy;
      var oldDeploymentsSortOrder = deploymentsSortOrder;
      var oldDeploymentId = deploymentId;
      var oldResourceId = resourceId;
      var oldResourceName = resourceName;

      deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
      deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
      deploymentId = getPropertyFromLocation('deployment');
      resourceId = getPropertyFromLocation('resource');
      resourceName = getPropertyFromLocation('resourceName');

      if (
        (deploymentsSortBy && oldDeploymentsSortBy !== deploymentsSortBy) ||
        (deploymentsSortOrder &&
          oldDeploymentsSortOrder !== deploymentsSortOrder)
      ) {
        repositoryData.changed('deploymentsSorting');
      } else if (deploymentId && oldDeploymentId !== deploymentId) {
        repositoryData.changed('currentDeployment');
      } else if (
        (resourceId && oldResourceId !== resourceId) ||
        (resourceName && oldResourceName !== resourceName)
      ) {
        repositoryData.changed('resourceId');
      }
    });
  }
];
