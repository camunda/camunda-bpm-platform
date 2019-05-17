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

var camTasklistVariables = require('./directives/cam-tasklist-variables');
var camTasklistVariablesDetailsModalCtrl = require('./modals/cam-tasklist-variables-detail-modal');

var ngModule = angular.module('tasklist.plugin.tasklistCard.variables', [
  'ui.bootstrap',
  'angularMoment'
]);

var tasklistCardVariablesPlugin = [
  'ViewsProvider',
  function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('tasklist.card', {
      id: 'tasklist-card-variables',
      template:
        '<div cam-tasklist-variables ' +
        'filter-properties="filterProperties" ' +
        'variables="task._embedded.variable" ' +
        'class="row variables"></div>',
      controller: function() {},
      priority: 200
    });
  }
];

ngModule.config(tasklistCardVariablesPlugin);
ngModule.directive('camTasklistVariables', camTasklistVariables);
ngModule.controller(
  'camTasklistVariablesDetailsModalCtrl',
  camTasklistVariablesDetailsModalCtrl
);

module.exports = ngModule;
