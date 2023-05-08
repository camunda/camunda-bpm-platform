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

var testModule = angular.module('myModule', [camCommonsUi.name]);

testModule.controller('testController', ['$scope', function($scope) {
  $scope.searches = [];
  $scope.validSearches = [];
  $scope.searches2 = [];
  $scope.validSearches2 = [];
  $scope.translations = {
    'inputPlaceholder': 'Search for Anything',
    'invalid': 'This search query is not valid',
    'deleteSearch': 'Remove search',
    'type': 'Type',
    'name': 'Property',
    'operator': 'Operator',
    'value': 'Value'
  };
  $scope.types = [
    {
      'id': {
        'key': 'PredefinedOperators',
        'value': 'Predefined Operators'
      },
      'operators': [
        {'key': 'eq',  'value': '='},
        {'key': 'lt',  'value': '<'},
        {'key': 'like','value': 'like'}
      ],
      default: true
    },
    {
      'id': {
        'key': 'EnforceDate',
        'value': 'Enforce Date'
      },
      'operators': [
        {'key': 'eq', 'value': '='}
      ],
      allowDates: true,
      enforceDates: true
    },
    {
      'id': {
        'key': 'variableOperator',
        'value': 'Variable Operator'
      },
      allowDates: true,
      extended: true,
      potentialNames: [
        {key:'key1', value:'Label (key1)'},
        {key:'key2', value:'Label2 (key2)'}
      ]
    },
    {
      'id': {
        'key': 'basicQuery',
        'value': 'Basic Query'
      },
      basic: true
    }
  ];
  $scope.types2 = [
    {
      'id': {
        'key': 'A',
        'value': 'A'
      },
      'groups': ['A']
    },
    {
      'id': {
        'key': 'B',
        'value': 'B'
      },
      'groups': ['B']
    },
    {
      'id': {
        'key': 'C',
        'value': 'C'
      }
    }
  ];
  $scope.operators =  {
    'undefined': [
      {'key': 'eq', 'value': '='},
      {'key': 'neq','value': '!='}
    ],
    'string': [
      {'key': 'eq',  'value': '='},
      {'key': 'like','value': 'like'}
    ],
    'number': [
      {'key': 'eq', 'value': '='},
      {'key': 'neq','value': '!='},
      {'key': 'gt', 'value': '>'},
      {'key': 'lt', 'value': '<'}
    ],
    'boolean': [
      {'key': 'eq', 'value': '='}
    ],
    'object': [
      {'key': 'eq', 'value': '='}
    ],
    'date': [
      {'key': 'Before', 'value': 'before'},
      {'key': 'After',  'value': 'after'}
    ]
  };
}]);

angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name]);
});
