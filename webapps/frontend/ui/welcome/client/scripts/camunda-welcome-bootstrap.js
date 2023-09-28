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

import '../styles/styles.less';

window.$ = window.jQuery = require('jquery');

import {
  requirejs,
  define,
  require as rjsrequire
} from 'exports-loader?exports=requirejs,define,require!requirejs/require';

window.define = define;
window.require = rjsrequire;
window.bust = CAMUNDA_VERSION; // eslint-disable-line

// camunda-welcome-bootstrap is copied as-is, so we have to inline everything
const appRoot = document.querySelector('base').getAttribute('app-root');
const baseImportPath = `${appRoot}/app/welcome/`;

requirejs.config({
  baseUrl: baseImportPath,
  urlArgs: `bust=${CAMUNDA_VERSION}` // eslint-disable-line
});

const loadConfig = (async function() {
  const configPath =
    baseImportPath + 'scripts/config.js?bust=' + new Date().getTime();
  const config = (await _import(configPath)).default; // eslint-disable-line

  window.camWelcomeConf = config;
  return config;
})();

define('camunda-welcome-bootstrap', function() {
  'use strict';
  const bootstrap = config => {
    requirejs.config({
      baseUrl: '../../../lib'
    });

    var camundaWelcomeUi = require('./camunda-welcome-ui');
    camundaWelcomeUi.exposePackages(window);

    requirejs([`${appRoot}/lib/globalize.js`], function(globalize) {
      globalize(
        requirejs,
        [
          'angular',
          'camunda-commons-ui',
          'camunda-bpm-sdk-js',
          'jquery',
          'angular-data-depend',
          'moment',
          'events'
        ],
        window
      );

      var pluginPackages = window.PLUGIN_PACKAGES || [];
      var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

      pluginPackages = pluginPackages.filter(el =>
        el.name.startsWith('welcome-plugin-legacy')
      );
      pluginDependencies = pluginDependencies.filter(el =>
        el.requirePackageName.startsWith('welcome-plugin-legacy')
      );

      pluginPackages.forEach(function(plugin) {
        var node = document.createElement('link');
        node.setAttribute('rel', 'stylesheet');
        node.setAttribute(
          'href',
          plugin.location + `/plugin.css?bust=${CAMUNDA_VERSION}` // eslint-disable-line
        );
        document.head.appendChild(node);
      });

      requirejs.config({
        packages: pluginPackages,
        baseUrl: './',
        paths: {
          ngDefine: `${appRoot}/lib/ngDefine`
        }
      });

      var dependencies = ['angular', 'ngDefine'].concat(
        pluginDependencies.map(function(plugin) {
          return plugin.requirePackageName;
        })
      );

      requirejs(dependencies, function(angular) {
        // we now loaded the welcome and the plugins, great
        // before we start initializing the welcome though (and leave the requirejs context),
        // lets see if we should load some custom scripts first

        if (config && config.csrfCookieName) {
          angular.module('cam.commons').config([
            '$httpProvider',
            function($httpProvider) {
              $httpProvider.defaults.xsrfCookieName = config.csrfCookieName;
            }
          ]);
        }

        if (typeof config !== 'undefined' && config.requireJsConfig) {
          var custom = config.requireJsConfig || {};

          // copy the relevant RequireJS configuration in a empty object
          // see: http://requirejs.org/docs/api.html#config
          var conf = {};
          [
            'baseUrl',
            'paths',
            'bundles',
            'shim',
            'map',
            'config',
            'packages',
            // 'nodeIdCompat',
            'waitSeconds',
            'context',
            // 'deps', // not relevant in this case
            'callback',
            'enforceDefine',
            'xhtml',
            'urlArgs',
            'scriptType'
            // 'skipDataMain' // not relevant either
          ].forEach(function(prop) {
            if (custom[prop]) {
              conf[prop] = custom[prop];
            }
          });

          // configure RequireJS
          requirejs.config({baseUrl: '../', ...conf});

          // load the dependencies and bootstrap the AngularJS application
          requirejs(custom.deps || [], function() {
            // create a AngularJS module (with possible AngularJS module dependencies)
            // on which the custom scripts can register their
            // directives, controllers, services and all when loaded
            angular.module('cam.welcome.custom', custom.ngDeps);

            clearAmdGlobals();

            // now that we loaded the plugins and the additional modules, we can finally
            // initialize Welcome
            camundaWelcomeUi.init(pluginDependencies);
          });
        } else {
          // for consistency, also create a empty module
          angular.module('cam.welcome.custom', []);

          // make sure that we are at the end of the require-js callback queue.
          // Why? => the plugins will also execute require(..) which will place new
          // entries into the queue.  if we bootstrap the angular app
          // synchronously, the plugins' require callbacks will not have been
          // executed yet and the angular modules provided by those plugins will
          // not have been defined yet. Placing a new require call here will put
          // the bootstrapping of the angular app at the end of the queue
          rjsrequire([], function() {
            clearAmdGlobals();
            camundaWelcomeUi.init(pluginDependencies);
          });
        }

        /**
         * Clearing AMD globals is necessary so third-party AMD scripts users
         * register via script tag are prevented from registration via RequireJS
         * and made global on the window object.
         */
        function clearAmdGlobals() {
          window.define = undefined;
          window.require = undefined;
        }
      });
    });
  };

  loadConfig.then(config => bootstrap(config));
});

requirejs(['camunda-welcome-bootstrap'], function() {});
