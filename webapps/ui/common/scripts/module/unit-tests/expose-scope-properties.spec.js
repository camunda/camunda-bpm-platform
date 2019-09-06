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
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common exposeScopeProperties service', function() {
  var exposeScopeProperties;
  var $scope;
  var target;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function(_exposeScopeProperties_) {
    exposeScopeProperties = _exposeScopeProperties_;

    $scope = {
      a: 1,
      b: 2
    };
    target = {};

    exposeScopeProperties($scope, target, ['a']);
  }));

  describe('target', function() {
    it('should be able to access exposed scope properties', function() {
      expect(target.a).to.eql($scope.a);
    });

    it('should be able to write to exposed scope properties', function() {
      target.a = 12;

      expect($scope.a).to.eql(12);

      $scope.a = 10;
      expect(target.a).to.eql(10);
    });

    it('should not be able to access other scope properties', function() {
      expect(target.b).not.to.eql($scope.b);
      expect(target.b).to.eql(undefined);
    });
  });
});
