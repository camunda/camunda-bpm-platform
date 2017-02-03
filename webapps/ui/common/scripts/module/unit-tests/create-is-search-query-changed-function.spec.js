'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('angular');
var camCommon = require('../index');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common createIsSearchQueryChangedFunction', function() {
  var search;
  var createIsSearchQueryChangedFunction;

  beforeEach(module(camCommon.name));

  beforeEach(module(function($provide) {
    search = sinon.stub().returns({
      searchQuery: 'something'
    });
    $provide.value('search', search);
  }));

  beforeEach(inject(function($injector) {
    createIsSearchQueryChangedFunction = $injector.get('createIsSearchQueryChangedFunction');
  }));

  it('should create function', function() {
    expect(typeof createIsSearchQueryChangedFunction()).to.eql('function');
  });

  it('should produce isolated functions', function() {
    var firstIsFn = createIsSearchQueryChangedFunction();
    var secondIsFn = createIsSearchQueryChangedFunction();

    // let's run this function just to make difference between instances
    firstIsFn();

    expect(
      Boolean(
        firstIsFn()
      )
    ).to.eql(false, 'expected first function not to dectect change');
    expect(secondIsFn())
      .to.eql(true, 'expected second function to dectect change');
  });

  describe('isSearchQueryChanged', function() {
    var isSearchQueryChanged;

    beforeEach(function() {
      isSearchQueryChanged = createIsSearchQueryChangedFunction();
    });

    it('should fetch current search params', function() {
      isSearchQueryChanged();

      expect(search.calledOnce)
        .to.eql(true, 'expected search to be called only once');
      expect(search.firstCall.args.length)
        .to.eql(0, 'expected search to be called without arguments');
    });

    it('should return true after first run with changed search query', function() {
      expect(isSearchQueryChanged()).to.eql(true);
    });

    it('should return falsy value after second run with same search query', function() {
      isSearchQueryChanged();

      expect(isSearchQueryChanged()).to.not.be.ok;
    });

    it('should return falsy value on first run when search query is empty', function() {
      search.returns({
        searchQuery: '[]'
      });

      expect(isSearchQueryChanged()).to.not.be.ok;
    });

    it('should return true on second run when search query was not empty but is now', function() {
      isSearchQueryChanged();

      search.returns({
        searchQuery: '[]'
      });

      expect(isSearchQueryChanged()).to.be.ok;
    });
  });
});
