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

// # camunda-commons-ui
// @name camunda-commons-ui
//
// This file is an entry point for modules which depends on
// camunda-commons-ui to get the libraries

/* jshint node: true */
'use strict';
var _ = require('lodash');

// ## requirejs
// @name camunda-commons-ui.requirejs
//
// This function is aimed to provide a common (but overridable)
// configuration for require.js tasks.
//
// @param options
// @param options.pathPrefix should be the path to
//                           camunda-commons-ui relative from
//                           the project running the task
function requirejsConf(options) {
  options = options || {};

  if (typeof options.pathPrefix === 'undefined') {
    options.pathPrefix = 'camunda-commons-ui';
  }

  var conf = {
    stubModules: ['text'],

    paths: {
      'camunda-commons-ui': 'lib',

      // #### npm dependencies
      'angular-data-depend': 'node_modules/angular-data-depend/src/dataDepend',
      'angular-translate':
        'node_modules/angular-translate/dist/angular-translate',
      'angular-moment': 'node_modules/angular-moment/angular-moment',
      'camunda-bpm-sdk-js':
        'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk-angular',
      'camunda-bpm-sdk-js-type-utils':
        'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk-type-utils',
      jquery: 'node_modules/jquery/dist/jquery',
      moment: 'node_modules/moment/moment',
      requirejs: 'node_modules/requirejs/require',
      ngDefine: 'node_modules/requirejs-angular-define/dist/ngDefine',
      text: 'node_modules/requirejs-text/text',
      lodash: 'node_modules/lodash/lodash',
      angular: 'node_modules/angular/angular',
      'angular-animate': 'node_modules/angular-animate/angular-animate',
      'angular-cookies': 'node_modules/angular-cookies/angular-cookies',
      'angular-loader': 'node_modules/angular-loader/angular-loader',
      'angular-resource': 'node_modules/angular-resource/angular-resource',
      'angular-route': 'node_modules/angular-route/angular-route',
      'angular-sanitize': 'node_modules/angular-sanitize/angular-sanitize',
      'angular-touch': 'node_modules/angular-touch/angular-touch',

      // #### vendor dependencies
      'angular-bootstrap': 'angular-ui-bootstrap',
      prismjs: 'vendor/prism',
      'bpmn-io': 'node_modules/bower-bpmn-js/dist/bpmn-navigated-viewer',
      'dmn-io': 'node_modules/camunda-dmn-js'
    },

    shim: {
      angular: {
        deps: ['jquery'],
        exports: 'angular'
      },

      'camunda-commons-ui': [
        'angular',
        'angular-resource',
        'angular-route',
        'angular-sanitize',
        'angular-translate',
        'angular-bootstrap',
        'moment',
        'placeholders-js'
      ],
      'angular-animate': ['angular'],
      'angular-cookies': ['angular'],
      'angular-loader': ['angular'],
      'angular-mocks': ['angular'],
      'angular-resource': ['angular'],
      'angular-route': ['angular'],
      'angular-sanitize': ['angular'],
      'angular-touch': ['angular'],
      'angular-bootstrap': ['angular'],
      'angular-translate': ['angular']
    },

    packages: [
      {
        name: 'camunda-commons-ui',
        location: 'lib',
        main: 'index'
      },
      {
        name: 'camunda-commons-ui/util',
        location: 'lib/util',
        main: 'index'
      }
    ]
  };

  // prefix all the paths
  _.each(conf.paths, function(val, key) {
    conf.paths[key] = options.pathPrefix + '/' + val;
  });
  _.each(conf.packages, function(val, key) {
    if (conf.packages[key].location) {
      conf.packages[key].location =
        options.pathPrefix + '/' + conf.packages[key].location;
    }
  });

  return conf;
}

module.exports = {
  // @name camunda-commons-ui.utils
  utils: {
    // @name camunda-commons-ui.utils._
    _: _
  },

  requirejs: requirejsConf
};
