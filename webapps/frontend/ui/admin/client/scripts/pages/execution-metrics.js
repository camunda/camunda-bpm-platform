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

const template = require('./execution-metrics.html?raw');
const CamSDK = require('camunda-bpm-sdk-js/lib/angularjs/index');
const moment = require('moment');
const angular = require('angular');

const debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
const debounceQuery = debouncePromiseFactory();

const fmtMonth = 'MMMM YYYY';
const fmtYear = 'DD/MM/YYYY';

const Controller = [
  '$scope',
  '$filter',
  'Uri',
  'camAPI',
  'fixDate',
  'configuration',
  'PluginMetricsResource',
  function(
    $scope,
    $filter,
    Uri,
    camAPI,
    fixDate,
    configuration,
    PluginMetricsResource
  ) {
    var MetricsResource = camAPI.resource('metrics');

    // date variables
    var now = new Date();
    var dateFilter = $filter('date');
    var dateFormat = 'yyyy-MM-dd';

    // initial scope data
    $scope.startDate = dateFilter(now.getFullYear() + '-01-01', dateFormat);
    $scope.endDate = dateFilter(now.getFullYear() + '-12-31', dateFormat);
    $scope.loadingState = 'INITIAL';
    $scope.loadingStateMonthly = 'INITIAL';
    $scope.loadingStateAnnual = 'INITIAL';
    $scope.alwaysShowUTWMetrics = configuration.getAlwaysShowUniqueTaskWorkerMetrics();
    $scope.showTaskWorkerMetric = $scope.alwaysShowUTWMetrics;
    $scope.metrics = {};

    $scope.monthlyUsage = {};
    $scope.yearlyUsage = {};
    $scope.displayLegacyMetrics = true;

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
        setLoadingError(`Supported pattern '${dateFormat}'.`);
      } else if (
        form.startDate.$error.dateValue ||
        form.endDate.$error.dateValue
      ) {
        setLoadingError('Invalid Date Value.');
      }
    }

    $scope.$watch('startDate', function(newValue, oldValue) {
      if (newValue !== oldValue) {
        $scope.startDate = fixDate($scope.startDate);
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
          name: 'task-users',
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

    $scope.getSubscriptionMonthStyle = label => {
      // TODO extract logic
      let lastPeriodEnd = moment($scope.startDate);
      while (
        lastPeriodEnd
          .clone()
          .add(1, 'years')
          .isBefore(moment())
      ) {
        lastPeriodEnd.add(1, 'years');
      }
      //lastPeriodEnd = lastPeriodEnd.subtract(1, 'milliseconds');

      const activeMonth = !moment(label, 'YYYY.MM').isBefore(
        lastPeriodEnd,
        'month'
      );
      return activeMonth ? {} : {opacity: 0.7};
    };

    $scope.formatSubscriptionYear = label => {
      // TODO i18n
      const date = moment(label, 'YYYY-MM-DD');
      return `${date.format(fmtYear)} to ${date
        .add(1, 'years')
        .format(fmtYear)}`;
    };

    $scope.$watch('displayLegacyMetrics', () => {
      $scope.loadMonthly();
    });

    function createGroupLabel(year, month) {
      if (!month) {
        return year;
      } else {
        return `${year}.${String(month).padStart(2, '0')}`;
      }
    }

    function prepareTableData(response, labelFormat, initialMap) {
      let metricsGroupMap = angular.copy(initialMap || {});
      for (const metricItem of response) {
        const label = createGroupLabel(
          metricItem.subscriptionYear,
          metricItem.subscriptionMonth
        );
        let labelFmt;
        if (angular.isFunction(labelFormat)) {
          labelFmt = labelFormat(label);
        }
        if (angular.isString(labelFormat)) {
          labelFmt = moment(label, 'YYYY.MM').format(labelFormat);
        }

        if (!metricsGroupMap[label]) {
          metricsGroupMap[label] = {};
          metricsGroupMap[label].label = label;
          metricsGroupMap[label].labelFmt = labelFmt;
        }
        metricsGroupMap[label][metricItem.metric] = {
          sum: metricItem.sum,
          sumFmt: metricItem.sum.toLocaleString() || 0
        };
      }
      const monthlyList = Object.values(metricsGroupMap).sort(
        (a, b) => b.label - a.label
      );
      return monthlyList;
    }

    $scope.loadMonthly = () => {
      $scope.loadingStateMonthly = 'LOADING';
      let metrics = [];
      if (!$scope.displayLegacyMetrics) {
        metrics.push('process-instances', 'decision-instances', 'task-users');
      }
      PluginMetricsResource.getAggregated({
        subscriptionStartDate: $scope.startDate,
        startDate: moment($scope.startDate)
          .subtract(1, 'years')
          .startOf('month')
          .format('YYYY-MM-DDT00:00:00'),
        groupBy: 'month',
        metrics: metrics.length > 0 ? Array.from(metrics).toString() : null
      }).$promise.then(
        monthlyUsages => {
          // last 12 months
          // TODO calculate with subscriptionStart!!!
          const initialMap = {};
          let date = moment();
          for (let i = 0; i < 12; i++) {
            const label = createGroupLabel(date.year(), date.month() + 1);
            initialMap[label] = {label, labelFmt: date.format(fmtMonth)};
            date = date.subtract(1, 'months');
          }

          $scope.monthlyUsage = prepareTableData(
            monthlyUsages,
            fmtMonth,
            initialMap
          );
          $scope.loadingStateMonthly = 'LOADED';
        },
        err => {
          $scope.loadingStateMonthly = 'ERROR';
          $scope.loadingError = err; // FIXME
        }
      );
    };

    $scope.loadAnnual = () => {
      $scope.loadingStateAnnual = 'LOADING';
      PluginMetricsResource.getAggregated({
        subscriptionStartDate: fixDate($scope.startDate),
        groupBy: 'year'
      }).$promise.then(
        annualUsage => {
          $scope.annualUsage = prepareTableData(
            annualUsage,
            $scope.formatSubscriptionYear
          );
          $scope.loadingStateAnnual = 'LOADED';
        },
        err => {
          $scope.loadingStateAnnual = 'ERROR';
          $scope.loadingError = err; // FIXME
        }
      );
    };

    var load = ($scope.load = function() {
      $scope.loadMonthly();
      $scope.loadAnnual();

      $scope.loadingState = 'LOADING';
      var series = {
        flowNodes: function(cb) {
          MetricsResource.sum(
            {
              name: 'flow-node-instances',
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
              name: 'process-instances',
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
              name: 'decision-instances',
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
      priority: 900
    });
  }
];
