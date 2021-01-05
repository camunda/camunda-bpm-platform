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
var sinon = require('sinon');
var expect = chai.expect;
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.plugin.decisionList.views.dashboard DecisionListController', function() {
  var $rootScope;
  var $q;
  var $scope;
  var $controller;
  var decisionList;

  beforeEach(module(testModule.name));

  beforeEach(inject(function(_$controller_, _$rootScope_, _$q_) {
    decisionList = {
      getDecisionsLists: sinon.stub()
    };

    $controller = _$controller_;
    $rootScope = _$rootScope_;
    $q = _$q_;

    $scope = $rootScope.$new();
  }));

  it('should set initial loadingState to LOADING', function() {
    decisionList.getDecisionsLists.returns($q.when({}));

    $controller('DecisionListController', {
      $scope: $scope,
      decisionList: decisionList
    });

    expect($scope.loadingState).to.eql('LOADING');
  });

  describe('on success', function() {
    var decisions;
    var drds;

    beforeEach(function() {
      decisions = [1, 2, 3, 4];
      drds = ['a', 'b'];

      decisionList.getDecisionsLists.returns(
        $q.when({
          decisions: decisions.slice(),
          drds: drds.slice()
        })
      );

      $controller('DecisionListController', {
        $scope: $scope,
        decisionList: decisionList
      });

      $rootScope.$digest();
    });

    it('should set loading state to LOADED', function() {
      expect($scope.loadingState).to.eql('LOADED');
    });

    it('should set decision properties', function() {
      expect($scope.decisions).to.eql(decisions);
      expect($scope.decisionCount).to.eql(decisions.length);
    });

    it('should set drd conditions', function() {
      expect($scope.drds).to.eql(drds);
      expect($scope.drdsCount).to.eql(drds.length);
    });
  });

  describe('on fail', function() {
    var message = 'error message';
    var error;

    beforeEach(function() {
      decisionList.getDecisionsLists.returns(
        $q.reject({
          message: message
        })
      );

      try {
        $controller('DecisionListController', {
          $scope: $scope,
          decisionList: decisionList
        });

        $rootScope.$digest();
      } catch (_error) {
        error = _error;
      }
    });

    it('should set loading state to ERROR', function() {
      expect($scope.loadingState).to.eql('ERROR');
    });

    it('should throw error with correct message', function() {
      expect(error.message).to.eql(message);
    });

    it('should set error.message as loadingError', function() {
      expect($scope.loadingError).to.eql(message);
    });
  });
});
