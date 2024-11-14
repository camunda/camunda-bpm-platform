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
const moment = require('moment');
const angular = require('angular');
const Chart = require('chart.js');

const debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
const debounceMonthly = debouncePromiseFactory();
const debounceAnnual = debouncePromiseFactory();

const fmtMonth = 'MMMM YYYY';
const fmtYear = 'DD/MM/YYYY';
const fmtDatePicker = 'yyyy-MM-dd';
const fmtRequest = 'YYYY-MM-DD';
const metrics = {
  PI: 'process-instances',
  DI: 'decision-instances',
  TU: 'task-users',
  FNI: 'flow-node-instances',
  EDE: 'executed-decision-elements'
};
const localConfContractStartDate = 'metricsContractStartDate';
const datasetMetricColors = {
  PI: 'hsl(230, 70%, 41%)',
  DI: 'hsl(302, 70%, 41%)',
  TU: 'hsl(14, 70%, 41%)',
  FNI: 'hsl(86, 70%, 41%)',
  EDE: 'hsl(158, 70%, 41%)'
};

const Controller = [
  '$scope',
  '$filter',
  'camAPI',
  'configuration',
  'localConf',
  'PluginMetricsResource',
  '$translate',
  function(
    $scope,
    $filter,
    camAPI,
    configuration,
    localConf,
    PluginMetricsResource,
    $translate
  ) {
    const telemetryResource = camAPI.resource('telemetry');
    const dateFilter = $filter('date');

    // initial scope data
    $scope.fmtDatePicker = fmtDatePicker;
    let startDate = localConf.get(localConfContractStartDate);
    if (startDate) {
      $scope.startDate = startDate;
    } else {
      startDate = moment()
        .startOf('year')
        .toDate();
    }
    $scope.startDate = dateFilter(startDate, fmtDatePicker);

    $scope.loadingStateMonthly = 'INITIAL';
    $scope.loadingStateAnnual = 'INITIAL';
    $scope.metrics = {};

    let monthlyMetricUsageMap = {};
    let monthlyMetricsArray = {};
    $scope.monthlyMetrics = [];
    $scope.annualMetrics = [];
    $scope.displayLegacyMetrics = false;
    $scope.datePickerOptions = {maxDate: moment().toDate()};

    telemetryResource.fetchData((err, res) => {
      if (!err) {
        $scope.telemetryData = res;
        delete $scope.telemetryData.product.internals.commands;
        delete $scope.telemetryData.product.internals.metrics;
      } else {
        $scope.telemetryData = $translate.instant(
          'DIAGNOSTICS_FETCH_DATA_ERROR_MESSAGE',
          {
            err
          }
        );
      }
    });

    const initChart = () => {
      const $canvas = angular.element('canvas#monthly-metrics-chart-canvas');
      const ctx = $canvas[0].getContext('2d');
      Chart.Chart.register(
        Chart.BarController,
        Chart.BarElement,
        Chart.LinearScale,
        Chart.CategoryScale,
        Chart.Legend,
        Chart.Tooltip
      );
      $scope.chart = new Chart.Chart(ctx, {
        type: 'bar',
        data: {
          labels: [],
          datasets: []
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: {
            mode: 'index',
            intersect: false
          }
        }
      });
    };

    const updateMonthlyChart = monthlyMetrics => {
      const createDataset = metricsKey => {
        let data = [];
        for (const monthlyMetric of monthlyMetrics) {
          data.push(monthlyMetric[metrics[metricsKey]]?.sum || 0);
        }
        return {
          label: metricsKey,
          data: data,
          backgroundColor: datasetMetricColors[metricsKey]
        };
      };

      let datasets = [];
      datasets.push(createDataset('PI'));
      datasets.push(createDataset('DI'));
      datasets.push(createDataset('TU'));
      if ($scope.displayLegacyMetrics) {
        datasets.push(createDataset('FNI'));
        datasets.push(createDataset('EDE'));
      }

      $scope.chart.data.labels = monthlyMetrics.map(a => a.labelFmt);
      $scope.chart.data.datasets = datasets;
      $scope.chart.update();
    };

    // sets loading state to error and updates error message
    const setInputError = error => {
      $scope.loadingStateMonthly = $scope.loadingStateAnnual = 'ERROR';
      $scope.loadingErrorMonthly = $scope.loadingErrorAnnual = error;
    };

    // called every time date input changes
    const handleDateChange = () => {
      const date = moment($scope.startDate, fmtRequest, true);
      if (date.isValid()) {
        localConf.set(localConfContractStartDate, $scope.startDate);
        calculateContractDates();
        return load();
      } else {
        setInputError(`Invalid Date Value. Supported pattern '${fmtRequest}'.`);
      }
    };

    $scope.getSubscriptionMonthStyle = metric => {
      return metric.activeYear ? {} : {opacity: 0.7};
    };

    const formatSubscriptionYear = label => {
      const date = moment($scope.startDate).year(label);
      const endDate = date.clone().add(1, 'years');
      const translationId = endDate.isAfter(moment())
        ? 'EXECUTION_METRICS_ANNUAL_FORMAT_CURRENT'
        : 'EXECUTION_METRICS_ANNUAL_FORMAT';

      return $translate.instant(translationId, {
        from: date.format(fmtYear),
        to: endDate.format(fmtYear)
      });
    };

    const createGroupLabel = (year, month) => {
      if (!month) {
        return year;
      } else {
        return `${year}.${String(month).padStart(2, '0')}`;
      }
    };

    const prepareTableData = (response, labelFormat, metricsGroupMap = {}) => {
      for (const metricUsage of response) {
        const label = createGroupLabel(
          metricUsage.subscriptionYear,
          metricUsage.subscriptionMonth
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
        metricsGroupMap[label][metricUsage.metric] = {
          sum: metricUsage.sum,
          sumFmt: metricUsage.sum.toLocaleString() || 0
        };
      }
      return metricsGroupMap;
    };

    const mapToList = metricsGroupMap => {
      // sort descending by date labels
      return Object.values(metricsGroupMap).sort((a, b) => b.label - a.label);
    };

    const getMonthlyMetrics = (metrics, startDate, endDate) => {
      return new Promise((resolve, reject) => {
        PluginMetricsResource.getAggregated({
          subscriptionStartDate: $scope.startDate,
          groupBy: 'month',
          metrics: Array.from(metrics).toString(),
          startDate,
          endDate
        }).$promise.then(
          monthlyMetrics => {
            prepareTableData(monthlyMetrics, fmtMonth, monthlyMetricUsageMap);
            resolve();
          },
          err => reject(err)
        );
      });
    };

    const calculateContractDates = () => {
      // calculate active subscription month
      let activeMonth = moment($scope.startDate).startOf('day');
      while (
        activeMonth
          .clone()
          .add(1, 'month')
          .isBefore(moment())
      ) {
        activeMonth.add(1, 'month');
      }
      $scope.activeMonth = activeMonth;
      // calculate active subscription year
      let activeYear = moment($scope.startDate).startOf('day');
      while (
        activeYear
          .clone()
          .add(1, 'year')
          .isBefore(moment())
      ) {
        activeYear.add(1, 'year');
      }
      $scope.activeYear = activeYear;
    };

    const initializeMonthlyData = () => {
      // prefill last 24 months for monthly table
      monthlyMetricUsageMap = {};
      let month = $scope.activeMonth.clone();
      for (let i = 0; i < 24; i++) {
        // create label
        const label = createGroupLabel(month.year(), month.month() + 1);
        monthlyMetricUsageMap[label] = {
          label,
          labelFmt: month.format(fmtMonth),
          activeYear: !month.isBefore($scope.activeYear)
        };

        // prefill data
        Object.values(metrics).forEach(metricName => {
          monthlyMetricUsageMap[label][metricName] = {
            sum: 0,
            sumFmt: 0
          };
        });

        month = month.subtract(1, 'months');
      }
    };

    const loadMonthly = () => {
      $scope.loadingStateMonthly = 'LOADING';

      initializeMonthlyData();

      // calculate query dates
      const prevSubStart = $scope.activeYear
        .clone()
        .subtract(1, 'year')
        .format(fmtRequest);
      const curSubStart = $scope.activeYear.format(fmtRequest);
      let requestMetrics = [metrics.PI, metrics.DI];
      if ($scope.displayLegacyMetrics) {
        requestMetrics.push(metrics.FNI, metrics.EDE);
      }

      let series = [
        // load regular metrics (non-TU)
        getMonthlyMetrics(requestMetrics, prevSubStart),
        // load TU metrics for current and last subscription year
        getMonthlyMetrics([metrics.TU], curSubStart),
        getMonthlyMetrics([metrics.TU], prevSubStart, curSubStart)
      ];

      debounceMonthly(Promise.all(series))
        .then(() => {
          monthlyMetricsArray = mapToList(monthlyMetricUsageMap);

          // accumulate TU metrics
          for (let i = monthlyMetricsArray.length - 1; i >= 0; i--) {
            const metric = monthlyMetricsArray[i];
            if (i - 1 >= 0) {
              // check if two items belong to the same subscription year
              const nextMetric = monthlyMetricsArray[i - 1];
              if (metric.activeYear === nextMetric.activeYear) {
                const sum = metric[metrics.TU].sum + nextMetric[metrics.TU].sum;
                nextMetric[metrics.TU].sum = sum;
                nextMetric[metrics.TU].sumFmt = sum.toLocaleString();
              }
            }
          }

          $scope.monthlyMetrics = angular.copy(monthlyMetricsArray);
          $scope.monthlyMetrics = $scope.monthlyMetrics.slice(0, 12);
          updateMonthlyChart($scope.monthlyMetrics.reverse());
          $scope.loadingStateMonthly = 'LOADED';
          $scope.$apply();
        })
        .catch(err => {
          $scope.loadingStateMonthly = 'ERROR';
          $scope.loadingErrorMonthly = getApiError(err);
        });
    };

    const loadAnnual = () => {
      $scope.loadingStateAnnual = 'LOADING';
      debounceAnnual(
        PluginMetricsResource.getAggregated({
          subscriptionStartDate: $scope.startDate,
          groupBy: 'year'
        }).$promise
      )
        .then(annualMetrics => {
          let annualMetricsMap = prepareTableData(
            annualMetrics,
            formatSubscriptionYear
          );
          // fill missing values
          for (const annualMetrics in annualMetricsMap) {
            Object.values(metrics).forEach(metricName => {
              if (!annualMetricsMap[annualMetrics][metricName]) {
                annualMetricsMap[annualMetrics][metricName] = {
                  sum: 0,
                  sumFmt: 0
                };
              }
            });
          }
          $scope.annualMetrics = mapToList(annualMetricsMap);
          $scope.loadingStateAnnual =
            $scope.annualMetrics.length === 0 ? 'EMPTY' : 'LOADED';
          $scope.$apply();
        })
        .catch(err => {
          $scope.loadingStateAnnual = 'ERROR';
          $scope.loadingErrorAnnual = getApiError(err);
        });
    };

    const getApiError = err => {
      let msg;
      if (err.data?.type) {
        msg = err.data.type;
        if (err.data.message) {
          msg += ': ' + err.data.message;
        }
      } else {
        msg = err.statusText;
      }

      return $translate.instant('EXECUTION_METRICS_FETCH_DATA_ERROR_MESSAGE', {
        msg
      });
    };

    $scope.getClipboardText = metric => {
      let str = '';
      str += `${metric.labelFmt}\n`;
      Object.keys(metrics).forEach(metricKey => {
        str += `- ${metricKey}: ${metric[metrics[metricKey]].sumFmt}\n`;
      });
      str += '\n';
      str += JSON.stringify($scope.telemetryData, null, 2);
      return str;
    };

    const load = ($scope.load = () => {
      loadMonthly();
      loadAnnual();
    });

    $scope.$watch('startDate', (newValue, oldValue) => {
      if (newValue !== oldValue) handleDateChange();
    });

    $scope.$watch('displayLegacyMetrics', (newValue, oldValue) => {
      if (newValue !== oldValue) loadMonthly();
    });

    initChart();
    calculateContractDates();
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
