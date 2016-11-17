'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common CamSearchAbleAreaController', function() {
  var $scope;
  var instance;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($controller) {
    $scope = {
      config: {},
      total: {},
      arrayTypes: [],
      variableTypes: [],
      loadingState: 'load',
      loadingError: 'error',
      onSearchChange: sinon.spy(),
      textEmpty: 'text-empty'
    };

    instance = $controller('CamSearchAbleAreaController', {
      $scope: $scope
    });
  }));

  it('should expose $scope properties', function() {
    expect(instance.config).to.equal($scope.config);
    expect(instance.total).to.equal($scope.total);
    expect(instance.arrayTypes).to.equal($scope.arrayTypes);
    expect(instance.variableTypes).to.equal($scope.variableTypes);
    expect(instance.loadingState).to.equal($scope.loadingState);
    expect(instance.loadingError).to.equal($scope.loadingError);
    expect(instance.onSearchChange).to.equal($scope.onSearchChange);
    expect(instance.textEmpty).to.equal($scope.textEmpty);
  });

  describe('onQueryChange', function() {
    var query;

    beforeEach(function() {
      query = {
        b: 1
      };

      instance.triggerSearchChange = sinon.spy();
    });

    it('should set the query on controller', function() {
      instance.onQueryChange(query);

      expect(instance.query).to.eql(query);
    });

    it('should trigger search change', function() {
      instance.onQueryChange(query);

      expect(instance.triggerSearchChange.calledOnce).to.eql(true);
    });
  });

  describe('onPaginationChange', function() {
    var pages;

    beforeEach(function() {
      pages = {
        b: 1
      };

      instance.triggerSearchChange = sinon.spy();
    });

    it('should set the pages on controller', function() {
      instance.onPaginationChange (pages);

      expect(instance.pages).to.eql(pages);
    });

    it('should trigger search change', function() {
      instance.onPaginationChange (pages);

      expect(instance.triggerSearchChange.calledOnce).to.eql(true);
    });
  });

  describe('triggerSearchChange', function() {
    var pages;
    var query;

    beforeEach(function() {
      query = {
        as: 12
      };

      pages = {
        bt: 405
      };
    });

    it('should not call onSearchChange if query is not set', function() {
      instance.pages = pages;
      instance.triggerSearchChange();

      expect(instance.onSearchChange.called).to.eql(false);
    });

    it('should not call onSearchChange if pages is not set', function() {
      instance.query = query;
      instance.triggerSearchChange();

      expect(instance.onSearchChange.called).to.eql(false);
    });

    it('should call onSearchChange when both pages and query are set', function() {
      instance.pages = pages;
      instance.query = query;
      instance.triggerSearchChange();

      expect(instance.onSearchChange.calledOnce).to.eql(true);
      expect(instance.onSearchChange.calledWith({
        query: query,
        pages: pages
      })).to.eql(true);
    });
  });
});
