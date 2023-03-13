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

/**
 * @namespace camunda.common
 */

'use strict';

var angular = require('../../camunda-bpm-sdk-js/vendor/angular'),
  analytics = require('./analytics/index'),
  auth = require('./auth/index'),
  util = require('./util/index'),
  pages = require('./pages/index'),
  plugin = require('./plugin/index'),
  directives = require('./directives/index'),
  resources = require('./resources/index'),
  search = require('./search/index'),
  services = require('./services/index'),
  widgets = require('./widgets/index'),
  dateFilter = require('./filter/date/index');

require('angular-ui-bootstrap');
require('angular-translate');
require('angular-cookies');
require('angular-animate');

module.exports = angular.module('cam.commons', [
  auth.name,
  analytics.name,
  util.name,
  pages.name,
  plugin.name,
  directives.name,
  resources.name,
  search.name,
  services.name,
  widgets.name,
  dateFilter.name,
  'ui.bootstrap',
  'pascalprecht.translate',
  'ngCookies',
  'ngAnimate'
]);
