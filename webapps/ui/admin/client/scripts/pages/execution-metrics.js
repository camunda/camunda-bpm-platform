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

var template = fs.readFileSync(__dirname + '/execution-metrics.html', 'utf8');
var CamSDK = require('camunda-bpm-sdk-js/lib/angularjs/index');

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debounceQuery = debouncePromiseFactory();

var Controller = [
  '$scope',
  '$filter',
  'Uri',
  'camAPI',
  'fixDate',
  function($scope, $filter, Uri, camAPI, fixDate) {
    var MetricsResource = camAPI.resource('metrics');

    // date variables
    var now = new Date();
    var dateFilter = $filter('date');
    var dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

    // initial scope data
    $scope.startDate = dateFilter(
      now.getFullYear() + '-01-01T00:00:00.000',
      dateFormat
    );
    $scope.endDate = dateFilter(
      now.getFullYear() + '-12-31T23:59:59.999',
      dateFormat
    );
    $scope.loadingState = 'INITIAL';

    // sets loading state to error and updates error message
    function setLoadingError(error) {
      $scope.loadingState = 'ERROR';
      $scope.loadingError = error;
    }

    // called every time date input changes
    function handleDateChange() {
      var form = $scope.form;
      if (form.$valid) {
        return load();
      } else if (
        form.startDate.$error.datePattern ||
        form.endDate.$error.datePattern
      ) {
        setLoadingError("Supported pattern 'yyyy-MM-ddTHH:mm:ss'.");
      } else if (
        form.startDate.$error.dateValue ||
        form.endDate.$error.dateValue
      ) {
        setLoadingError('Invalid Date Value.');
      }
    }

    $scope.$watch('startDate', function(newValue, oldValue) {
      if (newValue !== oldValue) {
        handleDateChange();
      }
    });

    $scope.$watch('endDate', function(newValue, oldValue) {
      if (newValue !== oldValue) {
        handleDateChange();
      }
    });

    function updateView() {
      var phase = $scope.$root.$$phase;
      if (phase !== '$apply' && phase !== '$digest') {
        $scope.$apply();
      }
    }

    function fetchTaskWorkerMetric(cb) {
      MetricsResource.sum(
        {
          name: 'unique-task-workers',
          startDate: fixDate($scope.startDate),
          endDate: fixDate($scope.endDate)
        },
        function(err, res) {
          cb(err, !err ? res.result : null);
        }
      );
    }

    $scope.$watch('showTaskWorkerMetric', function() {
      if ($scope.showTaskWorkerMetric) {
        $scope.loadingState = 'LOADING';
        fetchTaskWorkerMetric(function(err, result) {
          if (!err) {
            $scope.loadingState = 'LOADED';
            $scope.metrics.taskWorkers = result;
          } else {
            setLoadingError('Could not load task worker metrics.');
            $scope.loadingState = 'ERROR';
          }
          updateView();
        });
      } else if (typeof $scope.showTaskWorkerMetric !== 'undefined') {
        $scope.loadingState = 'LOADED';
      }
    });

    var load = ($scope.load = function() {
      $scope.loadingState = 'LOADING';
      var series = {
        flowNodes: function(cb) {
          MetricsResource.sum(
            {
              name: 'activity-instance-start',
              startDate: fixDate($scope.startDate),
              endDate: fixDate($scope.endDate)
            },
            function(err, res) {
              cb(err, !err ? res.result : null);
            }
          );
        },
        decisionElements: function(cb) {
          MetricsResource.sum(
            {
              name: 'executed-decision-elements',
              startDate: fixDate($scope.startDate),
              endDate: fixDate($scope.endDate)
            },
            function(err, res) {
              cb(err, !err ? res.result : null);
            }
          );
        },
        rootProcessInstances: function(cb) {
          MetricsResource.sum(
            {
              name: 'root-process-instance-start',
              startDate: fixDate($scope.startDate),
              endDate: fixDate($scope.endDate)
            },
            function(err, res) {
              cb(err, !err ? res.result : null);
            }
          );
        },
        decisionInstances: function(cb) {
          MetricsResource.sum(
            {
              name: 'executed-decision-instances',
              startDate: fixDate($scope.startDate),
              endDate: fixDate($scope.endDate)
            },
            function(err, res) {
              cb(err, !err ? res.result : null);
            }
          );
        }
      };

      if ($scope.showTaskWorkerMetric) {
        series.taskWorkers = fetchTaskWorkerMetric;
      } else {
        delete series.taskWorkers;
      }

      // promises??? YES!
      debounceQuery(CamSDK.utils.series(series))
        .then(function(res) {
          $scope.loadingState = 'LOADED';
          $scope.metrics = res;
          updateView();
        })
        .catch(function() {
          setLoadingError('Could not set start and end dates.');
          $scope.loadingState = 'ERROR';
          updateView();
          return;
        });
    });

    load();
  }
];

module.exports = [
  'ViewsProvider',
  function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('admin.system', {
      id: 'system-settings-metrics',
      label: 'EXECUTION_METRICS',
      template: template,
      controller: Controller,
      priority: 900,
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
      ]
    });
  }
];
