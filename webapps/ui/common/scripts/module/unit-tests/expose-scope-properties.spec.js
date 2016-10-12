'use strict';

var chai = require('chai');
var expect = chai.expect;
var angular = require('camunda-commons-ui/vendor/angular');
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
