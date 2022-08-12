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

import '../styles/styles.less';
import 'ui/cockpit/client/styles/styles-components.less';
import 'ui/cockpit/plugins/styles.less';

const $ = window.jQuery;

// DOM Polyfills
require('dom4');

require('camunda-commons-ui/vendor/bootstrap');

var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-bpm-sdk-js/lib/angularjs/index');
var dataDepend = require('angular-data-depend');
var camCommon = require('../../../common/scripts/module');
var moment = require('camunda-commons-ui/vendor/moment');
var events = require('events');
var lodash = require('camunda-commons-ui/vendor/lodash');

require('../../../common/scripts/module');

var APP_NAME = 'cam.cockpit';

var angular = require('camunda-commons-ui/vendor/angular');

export function init(pluginDependencies) {
  var ngDependencies = [
    'ng',
    'ngResource',
    'pascalprecht.translate',
    commons.name,
    require('./repository/main').name,
    require('./batches/main').name,
    require('./reports/main').name,
    require('./directives/main').name,
    require('./filters/main').name,
    require('./pages/main').name,
    require('./resources/main').name,
    require('./services/main').name,
    require('./navigation/main').name
  ].concat(
    pluginDependencies.map(function (el) {
      return el.ngModuleName;
    })
  );

  var appNgModule = angular.module(APP_NAME, ngDependencies);

  function getUri(id) {
    var uri = $('base').attr(id);
    if (!id) {
      throw new Error('Uri base for ' + id + ' could not be resolved');
    }

    return uri;
  }

  var ModuleConfig = [
    '$routeProvider',
    'UriProvider',
    '$uibModalProvider',
    '$uibTooltipProvider',
    '$locationProvider',
    '$animateProvider',
    '$qProvider',
    '$compileProvider',
    function (
      $routeProvider,
      UriProvider,
      $modalProvider,
      $tooltipProvider,
      $locationProvider,
      $animateProvider,
      $qProvider,
      $compileProvider
    ) {
      $compileProvider.aHrefSanitizationTrustedUrlList(
        /^\s*(https?|s?ftp|mailto|tel|file|blob):/
      );
      $routeProvider.otherwise({redirectTo: '/dashboard'});

      UriProvider.replace(':appName', 'cockpit');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
      UriProvider.replace(
        'tasklistbase://',
        getUri('app-root') + '/app/tasklist/'
      );
      UriProvider.replace('cockpit://', getUri('cockpit-api'));
      UriProvider.replace(
        'admin://',
        getUri('admin-api') || getUri('cockpit-api') + '../admin/'
      );
      UriProvider.replace('plugin://', getUri('cockpit-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [
        '$window',
        function ($window) {
          if (DEV_MODE) {
            return 'default';
          }
          var uri = $window.location.href;

          var match = uri.match(/\/app\/cockpit\/([\w-]+)(|\/)/);
          if (match) {
            return match[1];
          } else {
            throw new Error('no process engine selected');
          }
        }
      ]);

      $modalProvider.options = {
        animation: true,
        backdrop: true,
        keyboard: true
      };

      $tooltipProvider.options({
        animation: true,
        popupDelay: 100,
        appendToBody: true
      });

      $locationProvider.hashPrefix('');

      $animateProvider.classNameFilter(/angular-animate/);

      $qProvider.errorOnUnhandledRejections(false);
    }
  ];

  appNgModule.provider(
    'configuration',
    require('./../../../common/scripts/services/cam-configuration')(
      window.camCockpitConf,
      'Cockpit'
    )
  );
  appNgModule.config(ModuleConfig);

  require('./../../../common/scripts/services/locales')(
    appNgModule,
    getUri('app-root'),
    'cockpit'
  );

  appNgModule.config([
    'camDateFormatProvider',
    function (camDateFormatProvider) {
      var formats = {
        monthName: 'MMMM',
        day: 'DD',
        abbr: 'lll',
        normal: 'YYYY-MM-DD[T]HH:mm:ss', // yyyy-MM-dd'T'HH:mm:ss => 2013-01-23T14:42:45
        long: 'LLLL',
        short: 'LL'
      };

      for (var f in formats) {
        camDateFormatProvider.setDateFormat(formats[f], f);
      }
    }
  ]);

  require('../../../common/scripts/services/plugins/addPlugins')(
    window.camCockpitConf,
    appNgModule,
    'cockpit'
  ).then(() => {
    angular.bootstrap(document.documentElement, [
      appNgModule.name,
      'cam.cockpit.custom'
    ]);

    if (top !== window) {
      window.parent.postMessage({type: 'loadamd'}, '*');
    }
  });
};

export function exposePackages(container) {
  container.angular = angular;
  container.jquery = $;
  container['camunda-commons-ui'] = commons;
  container['camunda-bpm-sdk-js'] = sdk;
  container['angular-data-depend'] = dataDepend;
  container['moment'] = moment;
  container['events'] = events;
  container['cam-common'] = camCommon;
  container['lodash'] = lodash;
};

