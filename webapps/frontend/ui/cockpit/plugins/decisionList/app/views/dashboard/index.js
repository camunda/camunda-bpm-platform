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
var camCommon = require('cam-common');
var decisionList = require('./views/decision-list');
var DecisionListController = require('./controllers/decision-list');
var decisionListService = require('./services/decision-list');
var decisionsTableComponent = require('./components/decisions-table');

var ngModule = angular.module('cockpit.plugin.decisionList.views.dashboard', [
  camCommon.name
]);

ngModule.config(decisionList);

ngModule.factory('decisionList', decisionListService);

ngModule.controller('DecisionListController', DecisionListController);

ngModule.directive('decisionsTable', decisionsTableComponent);

module.exports = ngModule;
