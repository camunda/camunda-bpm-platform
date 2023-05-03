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

var angular = require('camunda-commons-ui/vendor/angular'),
  camTasklistForm = require('./directives/cam-tasklist-form'),
  camTasklistFormGeneric = require('./directives/cam-tasklist-form-generic'),
  camTasklistFormGenericVariables = require('./directives/cam-tasklist-form-generic-variables'),
  camTasklistFormEmbedded = require('./directives/cam-tasklist-form-embedded'),
  camTasklistFormExternal = require('./directives/cam-tasklist-form-external'),
  camTasklistFormCamunda = require('./directives/cam-tasklist-form-camunda'),
  camTasklistUniqueValue = require('./directives/cam-tasklist-unique-value');

var formModule = angular.module('cam.tasklist.form', ['ui.bootstrap']);

formModule.directive('camTasklistForm', camTasklistForm);
formModule.directive('camTasklistFormGeneric', camTasklistFormGeneric);
formModule.directive(
  'camTasklistFormGenericVariables',
  camTasklistFormGenericVariables
);
formModule.directive('camTasklistFormEmbedded', camTasklistFormEmbedded);
formModule.directive('camTasklistFormExternal', camTasklistFormExternal);
formModule.directive('camTasklistFormCamunda', camTasklistFormCamunda);

formModule.directive('camUniqueValue', camTasklistUniqueValue);

module.exports = formModule;
