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

var angular = require('camunda-bpm-sdk-js/vendor/angular');
var clipboardDefinition = require('../cam-widget-clipboard');

require('angular-ui-bootstrap');

var clipboardModule = angular.module('clipboardModule', [
  'ui.bootstrap'
]);
clipboardModule.directive('camWidgetClipboard', clipboardDefinition);


var testModule = angular.module('testModule', [clipboardModule.name]);
testModule.controller('testController', [
  '$scope',
  function(
    $scope
  ) {
    $scope.obj = {
      varToCopy: 'W00p! W00p!'
    };
  }]);


angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name]);
});
