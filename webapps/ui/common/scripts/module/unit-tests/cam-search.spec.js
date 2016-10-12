'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
var drdCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common CamSearchController', function() {
  var $scope;
  var searchWidgetUtils;
  var instance;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($controller, $rootScope) {
    $scope = $rootScope.$new();

    angular.extend($scope, {
      config: {
        searches: {}
      },
      onQueryChange: sinon.spy(),
      arrayTypes: ['a', 'b'],
      variableTypes: ['c']
    });

    searchWidgetUtils = {
      createSearchQueryForSearchWidget: sinon.stub().returnsArg(0)
    };

    instance = $controller('CamSearchController', {
      $scope: $scope,
      searchWidgetUtils: searchWidgetUtils
    });
  }));

  it('should expose $scope properties', function() {
    expect(instance.config).to.equal($scope.config);
    expect(instance.onQueryChange).to.equal($scope.onQueryChange);
    expect(instance.arrayTypes).to.equal($scope.arrayTypes);
    expect(instance.variableTypes).to.equal($scope.variableTypes);
  });

  it('should update query on $scope.config.searches change', function() {
    $scope.$digest(); //just to fire initial watch
    $scope.config.searches.a = 1;
    $scope.$digest();

    expect(instance.onQueryChange.calledWith({query: {a: 1}})).to.eql(true);
  });

  describe('createQuery', function() {
    var searches;

    beforeEach(function() {
      searches = {
        b: 1
      };
    });

    it('should return query for searches', function() {
      expect(instance.createQuery(searches)).to.be.defined;
    });

    it('should use searchWidgetUtils to transform searches', function() {
      instance.createQuery(searches);

      expect(searchWidgetUtils.createSearchQueryForSearchWidget.calledWith(
        searches, instance.arrayTypes, instance.variableTypes
      )).to.eql(true);
    });
  });

  describe('updateQuery', function()  {
    var newValue;
    var oldValue;

    beforeEach(function() {
      newValue = {
        a: 1
      };
      oldValue = {
        b: 1
      };
    });

    it('should call onQueryChange when value changes', function() {
      instance.updateQuery(newValue, oldValue);

      expect(instance.onQueryChange.calledOnce).to.eql(true);
      expect(instance.onQueryChange.calledWith({
        query: newValue
      })).to.eql(true);
    });

    it('should not call onQueryChange when value does not change', function() {
      instance.updateQuery(newValue, newValue);

      expect(instance.onQueryChange.called).to.eql(false);
    });
  });
});
