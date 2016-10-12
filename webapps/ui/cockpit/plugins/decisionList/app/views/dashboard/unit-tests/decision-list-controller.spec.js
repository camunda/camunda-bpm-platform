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

    $controller('DecisionListController',  {
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

      decisionList.getDecisionsLists.returns($q.when({
        decisions: decisions.slice(),
        drds: drds.slice()
      }));

      $controller('DecisionListController',  {
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
      decisionList.getDecisionsLists.returns($q.reject({
        message: message
      }));

      try {
        $controller('DecisionListController', {
          $scope: $scope,
          decisionList: decisionList
        });

        $rootScope.$digest();
      } catch(_error) {
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
