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
var camCommonsUi = require('../../index');
var testXML = require('./test.xml');

var testModule = angular.module('myModule', [camCommonsUi.name]);
testModule.factory('debounce', require('../../../services/debounce'));
testModule.controller('testController', ['$scope', function($scope) {
  $scope.diagramXML = testXML;

  $scope.control = {};

  $scope.selectedNodes = [];
  $scope.handleClick = function(element) {
    if(element.businessObject.$instanceOf('cmmn:PlanItem')) {
      if($scope.control.isHighlighted(element.id)) {
        $scope.control.clearHighlight(element.id);
        $scope.control.removeBadges(element.id);
        $scope.selectedNodes.splice($scope.selectedNodes.indexOf(element.id),1);
        $scope.$apply();
      } else {
        $scope.control.highlight(element.id);
        $scope.control.createBadge(element.id, {text: 'Test', tooltip: 'This is a tooltip'});
        $scope.selectedNodes.push(element.id);
        $scope.$apply();
      }
    }
  };

  $scope.hovering = [];
  $scope.mouseEnter = function(element) {
    $scope.hovering.push(element.id);
    $scope.$apply();
  };
  $scope.mouseLeave = function(element) {
    $scope.hovering.splice($scope.hovering.indexOf(element.id), 1);
    $scope.$apply();
  };


}]);


angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name]);
});
