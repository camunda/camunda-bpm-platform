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

if (process.env.NODE_ENV === 'development') {
  require('../../../common/scripts/util/dev-setup').setupDev();
}

const $ = window.jQuery;

// DOM Polyfills
require('dom4');

var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-bpm-sdk-js/lib/angularjs/index');
require('angular-data-depend');

var angular = require('camunda-commons-ui/vendor/angular');
var dataDepend = require('angular-data-depend');
var camCommon = require('../../../common/scripts/module');
var lodash = require('camunda-commons-ui/vendor/lodash');
const translatePaginationCtrls = require('../../../common/scripts/util/translate-pagination-ctrls');

/**
 * @namespace cam
 */

/**
 * @module cam.tasklist
 */

function bootstrapApp() {
  $(document).ready(function() {
    angular.bootstrap(document.documentElement, [
      'cam.tasklist',
      'cam.embedded.forms',
      'cam.tasklist.custom'
    ]);

    setTimeout(function() {
      var $aufocused = $('[autofocus]');
      if ($aufocused.length) {
        $aufocused[0].focus();
      }
    }, 300);
  });
}

export function init(pluginDependencies) {
  var ngDeps = [
    commons.name,
    'pascalprecht.translate',
    'ngRoute',
    'dataDepend',
    require('./tasklist/index').name,
    require('./task/index').name,
    require('./process/index').name,
    require('./navigation/index').name,
    require('./form/index').name,
    require('./filter/index').name,
    require('./api/index').name,
    require('./shortcuts/plugins/index').name
  ].concat(
    pluginDependencies.map(function(el) {
      return el.ngModuleName;
    })
  );

  function parseUriConfig() {
    var $baseTag = $('base');
    var config = {};
    var names = ['href', 'app-root', 'admin-api', 'tasklist-api', 'engine-api'];
    for (var i = 0; i < names.length; i++) {
      config[names[i]] = $baseTag.attr(names[i]);
    }
    return config;
  }

  var uriConfig = parseUriConfig();

  var tasklistApp = angular.module('cam.tasklist', ngDeps);

  var ModuleConfig = [
    '$uibModalProvider',
    '$uibTooltipProvider',
    '$locationProvider',
    '$animateProvider',
    '$qProvider',
    '$provide',
    function(
      $modalProvider,
      $tooltipProvider,
      $locationProvider,
      $animateProvider,
      $qProvider,
      $provide
    ) {
      translatePaginationCtrls($provide);
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

      $qProvider.errorOnUnhandledRejections(DEV_MODE); // eslint-disable-line
    }
  ];

  tasklistApp.config(ModuleConfig);

  tasklistApp.factory(
    'assignNotification',
    require('./services/cam-tasklist-assign-notification')
  );
  tasklistApp.provider(
    'configuration',
    require('./../../../common/scripts/services/cam-configuration')(
      window.camTasklistConf,
      'Tasklist'
    )
  );

  require('./../../../common/scripts/services/locales')(
    tasklistApp,
    uriConfig['app-root'],
    'tasklist'
  );
  require('./config/uris')(tasklistApp, uriConfig);

  tasklistApp.config(require('./config/routes'));
  tasklistApp.config(require('./config/date'));
  tasklistApp.config(require('./config/tooltip'));

  tasklistApp.controller(
    'camTasklistAppCtrl',
    require('./controller/cam-tasklist-app-ctrl')
  );
  tasklistApp.controller(
    'camTasklistViewCtrl',
    require('./controller/cam-tasklist-view-ctrl')
  );

  require('../../../common/scripts/services/plugins/addPlugins')(
    window.camTasklistConf,
    tasklistApp,
    'tasklist'
  ).then(bootstrapApp);
}

export function exposePackages(container) {
  container.angular = angular;
  container.jquery = $;
  container['camunda-commons-ui'] = commons;
  container['camunda-bpm-sdk-js'] = sdk;
  container['angular-data-depend'] = dataDepend;
  container['cam-common'] = camCommon;
  container['lodash'] = lodash;
}
