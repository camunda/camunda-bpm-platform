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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  email = require('./email'),
  engineSelect = require('./engineSelect'),
  autoFill = require('./autoFill'),
  inPlaceTextField = require('./inPlaceTextField'),
  notificationsPanel = require('./notificationsPanel'),
  passwordRepeat = require('./passwordRepeat'),
  showIfAuthorized = require('./showIfAuthorized'),
  nl2br = require('./nl2br'),
  instantTypeahead = require('./instantTypeahead'),
  util = require('../util/index');

require('angular-ui-bootstrap');

var directivesModule = angular.module('camunda.common.directives', [
  'ui.bootstrap',
  util.name
]);

directivesModule.directive('email', email);
directivesModule.directive('autoFill', autoFill);
directivesModule.directive('engineSelect', engineSelect);
directivesModule.directive('camInPlaceTextField', inPlaceTextField);
directivesModule.directive('notificationsPanel', notificationsPanel);
directivesModule.directive('passwordRepeat', passwordRepeat);
directivesModule.directive('showIfAuthorized', showIfAuthorized);
directivesModule.directive('nl2br', nl2br);
directivesModule.directive('instantTypeahead', instantTypeahead);

directivesModule.config([
  '$uibModalProvider',
  '$uibTooltipProvider',
  function($modalProvider, $tooltipProvider) {
    $modalProvider.options = {
      animation: true,
      backdrop: true, //can be also false or 'static'
      keyboard: true
    };

    $tooltipProvider.options({
      animation: true,
      popupDelay: 100,
      appendToBody: true
    });
  }
]);

module.exports = directivesModule;
