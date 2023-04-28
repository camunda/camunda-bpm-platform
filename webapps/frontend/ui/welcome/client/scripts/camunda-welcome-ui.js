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

/* jshint browserify: true */

var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-bpm-sdk-js/lib/angularjs/index');
var camCommon = require('../../../common/scripts/module');
var lodash = require('camunda-commons-ui/vendor/lodash');

var APP_NAME = 'cam.welcome';

var angular = require('camunda-commons-ui/vendor/angular');
var pagesModule = require('./pages/main');
var directivesModule = require('./directives/main');
var servicesModule = require('./services/main');
var pluginsModule = require('./plugins/main');
const translatePaginationCtrls = require('../../../common/scripts/util/translate-pagination-ctrls');

export function init(pluginDependencies) {
  var ngDependencies = [
    'ng',
    'ngResource',
    'pascalprecht.translate',
    commons.name,
    pagesModule.name,
    directivesModule.name,
    servicesModule.name,
    pluginsModule.name
  ].concat(
    pluginDependencies.map(function(el) {
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
    '$animateProvider',
    '$qProvider',
    '$provide',
    function(
      $routeProvider,
      UriProvider,
      $animateProvider,
      $qProvider,
      $provide
    ) {
      translatePaginationCtrls($provide);
      $routeProvider.otherwise({redirectTo: '/welcome'});

      UriProvider.replace(':appRoot', getUri('app-root'));
      UriProvider.replace(':appName', 'welcome');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
      UriProvider.replace('welcome://', getUri('welcome-api'));
      UriProvider.replace('admin://', getUri('admin-api') + '../admin/');
      UriProvider.replace('plugin://', getUri('welcome-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [
        '$window',
        function($window) {
          var uri = $window.location.href;

          var match = uri.match(/\/app\/welcome\/([\w-]+)(|\/)/);
          if (match) {
            return match[1];
          } else {
            throw new Error('no process engine selected');
          }
        }
      ]);

      $animateProvider.classNameFilter(/angular-animate/);

      $qProvider.errorOnUnhandledRejections(DEV_MODE); // eslint-disable-line
    }
  ];

  appNgModule.provider(
    'configuration',
    require('./../../../common/scripts/services/cam-configuration')(
      window.camWelcomeConf,
      'Welcome'
    )
  );
  appNgModule.controller('WelcomePage', require('./controllers/welcome-page'));

  appNgModule.config(ModuleConfig);

  require('./../../../common/scripts/services/locales')(
    appNgModule,
    getUri('app-root'),
    'welcome'
  );

  require('../../../common/scripts/services/plugins/addPlugins')(
    window.camWelcomeConf,
    appNgModule,
    'welcome'
  ).then(() => {
    angular.bootstrap(document.documentElement, [
      appNgModule.name,
      'cam.welcome.custom'
    ]);

    if (top !== window) {
      window.parent.postMessage({type: 'loadamd'}, '*');
    }
  });
}

export function exposePackages(container) {
  container.angular = angular;
  container.jquery = $;
  container['camunda-commons-ui'] = commons;
  container['camunda-bpm-sdk-js'] = sdk;
  container['cam-common'] = camCommon;
  container['lodash'] = lodash;
}
