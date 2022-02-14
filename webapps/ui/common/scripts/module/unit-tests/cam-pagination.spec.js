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

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common CamPaginationController', function() {
  var search;
  var paginationUtils;
  var exposeScopeProperties;
  var $scope;
  var instance;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($controller) {
    $scope = {
      pages: 'pages',
      onPaginationChange: sinon.spy()
    };
    search = 'search';
    paginationUtils = {
      initializePaginationInController: sinon.spy()
    };
    exposeScopeProperties = sinon.spy();

    instance = $controller('CamPaginationController', {
      $scope: $scope,
      search: search,
      paginationUtils: paginationUtils,
      exposeScopeProperties: exposeScopeProperties
    });
  }));

  it('should initialize pagination', function() {
    expect(paginationUtils.initializePaginationInController.calledOnce).to.eql(
      true
    );
  });

  it('should expose total and pages scope properties', function() {
    expect(
      exposeScopeProperties.calledWith($scope, instance, ['total', 'pages'])
    ).to.eql(true);
  });

  it('should fire initial pages change', function() {
    expect($scope.onPaginationChange.calledWith({pages: $scope.pages})).to.eql(
      true
    );
  });
});
