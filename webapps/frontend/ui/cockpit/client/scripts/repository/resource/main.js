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
  /* controllers */
  camResourceDetailsCtrl = require('./controllers/cam-cockpit-resource-details-ctrl'),
  /* directives */
  camCockpitResourceWrapper = require('./directives/cam-cockpit-resource-wrapper'),
  camCockpitResourceMeta = require('./directives/cam-cockpit-resource-meta'),
  camCockpitResourceContent = require('./directives/cam-cockpit-resource-content'),
  camCockpitHtmlSource = require('./directives/cam-cockpit-html-source'),
  camCockpitSource = require('./directives/cam-cockpit-source'),
  camCockpitForm = require('./directives/cam-cockpit-form'),
  /* plugins */
  camCockpitDefinitionsPlugin = require('./plugins/details/definitions/cam-cockpit-definitions-plugin'),
  camCockpitResourceDownloadPlugin = require('./plugins/actions/download/cam-cockpit-resource-action-download-plugin');

var resourceModule = angular.module('cam.cockpit.repository.resource', []);

/* controllers */
resourceModule.controller('camResourceDetailsCtrl', camResourceDetailsCtrl);

/* directives */
resourceModule.directive('camResourceWrapper', camCockpitResourceWrapper);
resourceModule.directive('camResourceMeta', camCockpitResourceMeta);
resourceModule.directive('camResourceContent', camCockpitResourceContent);
resourceModule.directive('camForm', camCockpitForm);
resourceModule.directive('camSource', camCockpitSource);
resourceModule.directive('camHtmlSource', camCockpitHtmlSource);

/* plugins */
resourceModule.config(camCockpitDefinitionsPlugin);
resourceModule.config(camCockpitResourceDownloadPlugin);

module.exports = resourceModule;
