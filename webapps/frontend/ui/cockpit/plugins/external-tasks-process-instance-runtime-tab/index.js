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

var angular = require('../../../../camunda-commons-ui/vendor/angular');
var camCommon = require('ui/common/scripts/module/index');

var externalTasks = require('./services/external-tasks');

var ProcessInstanceRuntimeTabController = require('./controllers/process-instance-runtime-external-tasks-controller');

var viewConfig = require('./view-provider.config');

var ngModule = angular.module('cockpit.plugin.process-instance-runtime-tab', [
  camCommon.name
]);

ngModule.factory('externalTasks', externalTasks);

ngModule.controller(
  'ProcessInstanceRuntimeTabController',
  ProcessInstanceRuntimeTabController
);

ngModule.config(viewConfig);

module.exports = ngModule;
