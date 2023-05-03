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
    camCommonsUi = require('../../index');

var ngModule = angular.module('myModule', [camCommonsUi.name]);

ngModule.controller('testController', ['$scope', function($scope) {
  $scope.search1 = {
    type: {
      values: [{key: 'foo', value: 'Foo'}, {key: 'bar', value: 'Bar'}],
      value: null
    },
    operator: {
      values: [{key:'eq', value: '='}, {key:'neq', value:'!='}],
      value: null
    },
    value: {
      value: ''
    },
    valid: false,
    update: function() {
      $scope.search1.valid =
        $scope.search1.type.value !== null &&
        $scope.search1.operator.value !== null &&
        $scope.search1.value.value !== '';
    }
  };

  $scope.singleOperator = {
    type: {
      values: [{key: 'foo', value: 'Foo'}, {key: 'bar', value: 'Bar'}],
      value: null
    },
    operator: {
      values: [{key:'eq', value: '='}],
      value: null
    },
    value: {
      value: ''
    },
    valid: false,
    update: function() {
      $scope.singleOperator.valid =
        $scope.singleOperator.type.value !== null &&
        $scope.singleOperator.operator.value !== null &&
        $scope.singleOperator.value.value !== '';
    }
  };

  $scope.search2 = angular.copy($scope.search1);
  $scope.search2.value.value = '2015-01-08T12:46:35';
  $scope.search2.enforceDates = true;

  $scope.search3 = angular.copy($scope.search1);
  $scope.search3.allowDates = true;

  $scope.search4 = angular.copy($scope.search1);
  $scope.search4.name = {
    value: ''
  };
  $scope.search4.update = function() {
    $scope.search4.extended = $scope.search4.type.value && $scope.search4.type.value.key === 'bar';
  };

  $scope.search5 = angular.copy($scope.search1);
  $scope.search5.potentialNames = [
    {key:'name1', value:'Value 1 (name1)'},
    {key:'name2', value:'Value 2 (name2)'}
  ];
  $scope.search5.name = {
    value: ''
  };
  $scope.search5.extended = true;

  $scope.search6 = angular.copy($scope.search1);
  $scope.search6.basic = true;

  $scope.search7 = angular.copy($scope.search1);
  $scope.search7.enforceString = true;

  $scope.search8 = angular.copy($scope.search1);
  $scope.search8.options = ['yes', 'maybe', 'no'];
}]);

angular.element(document).ready(function() {
  angular.bootstrap(document.body, [ngModule.name]);
});
