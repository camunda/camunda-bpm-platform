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

var angular = require('angular'),
  // override job priority action
  overrideJobPriorityAction = require('./override-job-priority/override-job-priority-action'),
  overrideJobPriorityDialog = require('./override-job-priority/override-job-priority-dialog'),
  // bulk override job priority action
  bulkOverrideJobPriorityAction = require('./bulk-override-job-priority/bulk-override-job-priority-action'),
  bulkOverrideJobPriorityDialog = require('./bulk-override-job-priority/bulk-override-job-priority-dialog');

var ngModule = angular.module('cockpit.plugin.jobDefinition.actions', []);

// override job priority action
ngModule.config(overrideJobPriorityAction);
ngModule.controller(
  'JobDefinitionOverrideJobPriorityController',
  overrideJobPriorityDialog
);

// bulk override job priority action
ngModule.config(bulkOverrideJobPriorityAction);
ngModule.controller(
  'BulkJobDefinitionOverrideJobPriorityController',
  bulkOverrideJobPriorityDialog
);

module.exports = ngModule;
