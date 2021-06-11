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

var angular = require('angular');

var defaultConfig = {
  dateFormat: {
    monthName: 'MMMM',
    day: 'DD',
    abbr: 'lll',
    normal: 'LLL',
    long: 'LLLL',
    short: 'LL'
  },
  locales: {
    availableLocales: ['en'],
    fallbackLocale: 'en'
  },
  skipCustomListeners: {
    default: true,
    hidden: false
  },
  skipIoMappings: {
    default: true,
    hidden: false
  },
  runtimeActivityInstanceMetrics: {
    display: true
  },
  historicActivityInstanceMetrics: {
    adjustablePeriod: true,
    period: {
      unit: 'day'
    }
  },
  batchOperation: {
    mode: 'filter',
    autoLoadEnded: true
  },
  csrfCookieName: 'XSRF-TOKEN',
  disableWelcomeMessage: false,
  userOperationLogAnnotationLength: 4000,
  previewHtml: true,
  assignProcessInstanceIdToTaskComment: false
};

module.exports = function(config, app) {
  return [
    function() {
      var storage = window.localStorage;
      var values = JSON.parse(storage.getItem('camunda-web') || '{}');

      this.get = function(key, defaultValue) {
        return typeof values[key] !== 'undefined' ? values[key] : defaultValue;
      };

      this.set = function(key, value) {
        values[key] = value;
        storage.setItem('camunda-web', JSON.stringify(values));
      };

      // Removes old translations - default localStorage is limited to 10MB
      this.clearTranslationData = function() {
        for (var key in values) {
          if (key.includes('_locales_data_') && !key.includes(window.bust)) {
            delete values[key];
          }
        }

        window.localStorage.setItem('camunda-web', JSON.stringify(values));
      };

      this.getDateFormat = function(formatName) {
        var dateFormatObj = config.dateFormat || defaultConfig.dateFormat;
        return (
          dateFormatObj[formatName] || defaultConfig.dateFormat[formatName]
        );
      };

      this.getFallbackLocale = function() {
        if (config.locales && config.locales.fallbackLocale) {
          return config.locales.fallbackLocale;
        } else {
          return defaultConfig.locales.fallbackLocale;
        }
      };

      this.getAvailableLocales = function() {
        if (config.locales && config.locales.availableLocales) {
          return config.locales.availableLocales;
        } else {
          return defaultConfig.locales.availableLocales;
        }
      };

      this.getDateLocales = function() {
        return config.camDateLocales;
      };

      this.getAppVendor = function() {
        return config.app && config.app.vendor ? config.app.vendor : 'Camunda';
      };

      this.getAppName = function() {
        return config.app && config.app.name ? config.app.name : app;
      };

      this.getSkipCustomListeners = function() {
        return angular.extend(
          {},
          defaultConfig.skipCustomListeners,
          config.skipCustomListeners
        );
      };

      this.getSkipIoMappings = function() {
        return angular.extend(
          {},
          defaultConfig.skipIoMappings,
          config.skipIoMappings
        );
      };

      this.getRuntimeActivityInstanceMetrics = function() {
        var param = 'runtimeActivityInstanceMetrics';
        return angular.extend({}, defaultConfig[param], config[param]).display;
      };

      this.getActivityInstancePeriod = function() {
        var param = 'historicActivityInstanceMetrics';
        return config[param] && config[param].period
          ? config[param].period
          : defaultConfig[param].period;
      };

      this.getActivityInstanceAdjustable = function() {
        var param = 'historicActivityInstanceMetrics';

        return config[param] &&
          typeof config[param].adjustablePeriod !== 'undefined'
          ? config[param].adjustablePeriod
          : defaultConfig[param].adjustablePeriod;
      };

      this.getBatchOperationMode = function() {
        var param = 'batchOperation';
        return (
          (config[param] && config[param].mode) || defaultConfig[param].mode
        );
      };

      this.getBatchOperationAutoLoadEnded = function() {
        var param = 'batchOperation';

        return config[param] &&
          typeof config[param].autoLoadEnded !== 'undefined'
          ? config[param].autoLoadEnded
          : defaultConfig[param].autoLoadEnded;
      };

      this.getBpmnJs = function() {
        return config['bpmnJs'];
      };

      this.getHistoricProcessInstancesSearch = function() {
        return (config['defaultFilter'] || {})[
          'historicProcessDefinitionInstancesSearch'
        ];
      };

      this.getCsrfCookieName = function() {
        var param = 'csrfCookieName';
        return config[param] || defaultConfig[param];
      };

      this.getDisableWelcomeMessage = function() {
        var param = 'disableWelcomeMessage';
        return config[param] || defaultConfig[param];
      };

      this.getUserOperationLogAnnotationLength = function() {
        var param = 'userOperationLogAnnotationLength';
        return config[param] || defaultConfig[param];
      };

      this.getPreviewHtml = function() {
        var param = 'previewHtml';
        // 'false' is a valid config option
        return typeof config[param] !== 'undefined'
          ? config[param]
          : defaultConfig[param];
      };

      this.getAssignProcessInstanceIdToTaskComment = function() {
        var param = 'assignProcessInstanceIdToTaskComment';
        // 'false' is a valid config option
        return typeof config[param] !== 'undefined'
          ? config[param]
          : defaultConfig[param];
      };

      this.$get = function() {
        return this;
      };
    }
  ];
};
