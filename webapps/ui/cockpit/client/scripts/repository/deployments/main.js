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
  /* controller */
  camCockpitDeploymentsCtrl = require('./controllers/cam-cockpit-deployments-ctrl'),
  /* directives */
  camCockpitDeployments = require('./directives/cam-cockpit-deployments'),
  camCockpitDeployment = require('./directives/cam-cockpit-deployment'),
  camCockpitDeploymentsSortingChoices = require('./directives/cam-cockpit-deployments-sorting-choices'),
  /* plugins */
  camCockpitDeleteDeploymentPlugin = require('./plugins/actions/delete/cam-cockpit-delete-deployment-plugin'),
  /* modals */
  camCockpitDeleteDeploymentModalCtrl = require('./plugins/actions/delete/modals/cam-cockpit-delete-deployment-modal-ctrl');

var deploymentsModule = angular.module('cam.cockpit.repository.deployments', [
  'ui.bootstrap'
]);

/* controllers */
deploymentsModule.controller('camDeploymentsCtrl', camCockpitDeploymentsCtrl);

/* directives */
deploymentsModule.directive('camDeployments', camCockpitDeployments);
deploymentsModule.directive('camDeployment', camCockpitDeployment);
deploymentsModule.directive(
  'camDeploymentsSortingChoices',
  camCockpitDeploymentsSortingChoices
);

/* plugins */
deploymentsModule.config(camCockpitDeleteDeploymentPlugin);

/* modals */
deploymentsModule.controller(
  'camDeleteDeploymentModalCtrl',
  camCockpitDeleteDeploymentModalCtrl
);

module.exports = deploymentsModule;
