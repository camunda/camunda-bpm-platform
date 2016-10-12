'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
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
    expect(paginationUtils.initializePaginationInController.calledOnce).to.eql(true);
  });

  it('should expose total and pages scope properties', function() {
    expect(exposeScopeProperties.calledWith($scope, instance,  ['total', 'pages'])).to.eql(true);
  });

  it('should fire initial pages change', function() {
    expect($scope.onPaginationChange.calledWith({pages: $scope.pages})).to.eql(true);
  });
});
