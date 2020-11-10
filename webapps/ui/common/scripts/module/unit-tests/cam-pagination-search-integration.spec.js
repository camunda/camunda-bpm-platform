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

describe('cam-common CamPaginationSearchIntegrationController', function() {
  var $rootScope;
  var $q;
  var $scope;
  var search;
  var instance;
  var searchWidgetUtils;

  beforeEach(module(drdCommon.name));

  beforeEach(inject(function($injector) {
    var $controller = $injector.get('$controller');

    $rootScope = $injector.get('$rootScope');
    $q = $injector.get('$q');
    $scope = $rootScope.$new();

    $scope.config = {};
    $scope.total = {};
    $scope.arrayTypes = ['a'];
    $scope.variableTypes = ['b'];
    $scope.loadingState = 'load';
    $scope.loadingError = 'error';
    $scope.onSearchChange = sinon.spy();
    $scope.textEmpty = 'text-empty';

    search = sinon.stub();
    search.updateSilently = sinon.spy();

    search.returns({
      page: 2
    });

    searchWidgetUtils = {
      createSearchQueryForSearchWidget: sinon.stub()
    };

    instance = $controller('CamPaginationSearchIntegrationController', {
      $scope: $scope,
      search: search,
      searchWidgetUtils: searchWidgetUtils
    });
  }));

  it('should expose $scope properties', function() {
    expect(instance.config).to.equal($scope.config);
    expect(instance.arrayTypes).to.equal($scope.arrayTypes);
    expect(instance.variableTypes).to.equal($scope.variableTypes);
    expect(instance.loadingState).to.equal($scope.loadingState);
    expect(instance.loadingError).to.equal($scope.loadingError);
    expect(instance.onSearchChange).to.equal($scope.onSearchChange);
    expect(instance.textEmpty).to.equal($scope.textEmpty);
  });

  it('should initialize pages', function() {
    expect(instance.pages).to.eql({
      size: 50,
      total: 0,
      current: 2
    });
  });

  it('should watch current page change', function() {
    $scope.$digest();

    instance.getCurrentPageFromSearch = sinon.spy();
    instance.pages.current = Math.random();

    $scope.$digest();

    expect(instance.getCurrentPageFromSearch.calledOnce).to.eql(true);
  });

  it('should watch $locationChangeSuccess event', function() {
    instance.getCurrentPageFromSearch = sinon.spy();

    $scope.$broadcast('$locationChangeSuccess');

    expect(instance.getCurrentPageFromSearch.calledOnce).to.eql(true);
  });

  it('should watch config search changes', function() {
    $scope.$digest();

    instance.areSearchesDifferent = sinon.spy();
    $scope.config.searches = 'dd';

    $scope.$digest();

    expect(instance.areSearchesDifferent.calledOnce).to.eql(true);
  });

  it('should watch blocked flag', function() {
    $scope.$digest();

    instance.executeQueries = sinon.spy();
    instance.query = 'dd';

    $scope.blocked = true;
    $scope.$digest();

    $scope.blocked = false;
    $scope.$digest();

    expect(instance.executeQueries.calledOnce).to.eql(true);
  });

  it('should watch global cam-common:cam-searchable:query-force-change event', function() {
    instance.resetPage = sinon.spy();
    instance.executeQueries = sinon.spy();

    $rootScope.$broadcast('cam-common:cam-searchable:query-force-change');

    expect(instance.resetPage.calledOnce).to.eql(true);
    expect(instance.executeQueries.calledOnce).to.eql(true);
  });

  describe('onBlockedChange', function() {
    beforeEach(function() {
      instance.executeQueries = sinon.spy();
      instance.query = 'f';
    });

    it('should not trigger query when changes from not blocked to blocked', function() {
      instance.onBlockedChange(true, false);

      expect(instance.executeQueries.called).to.eql(false);
    });

    it('should trigger query when changes from blocked to not blocked', function() {
      instance.onBlockedChange(false, true);

      expect(instance.executeQueries.calledOnce).to.eql(true);
    });
  });

  describe('getSearchQueryString', function() {
    it('should return search string', function() {
      instance.search = sinon.stub().returns({
        searchQuery: 's1'
      });

      expect(instance.getSearchQueryString()).to.eql('s1');
    });
  });

  describe('hasSearchQueryStringChanged', function() {
    beforeEach(function() {
      instance.search = sinon.stub().returns({
        searchQuery: 's1'
      });
    });

    it('should return truthy when search query changed and is not empty', function() {
      instance.lastSearchQueryString = 's2';

      expect(Boolean(instance.hasSearchQueryStringChanged())).to.eql(true);
    });

    it('should return falsy when last query is undefined and query is empty', function() {
      instance.search = sinon.stub().returns({
        searchQuery: '[]'
      });

      expect(Boolean(instance.hasSearchQueryStringChanged())).to.eql(false);
    });

    it('should return false when last query is the same as current', function() {
      instance.lastSearchQueryString = 's1';

      expect(Boolean(instance.hasSearchQueryStringChanged())).to.eql(false);
    });
  });

  describe('onPageChange', function() {
    beforeEach(function() {
      instance.hasSearchQueryStringChanged = sinon.stub().returns(false);
      instance.getCurrentPageFromSearch = sinon.stub().returns(4);
      instance.executeQueries = sinon.spy();
    });

    it('should execute queries when page changes and update search', function() {
      instance.onPageChange(3, 5);

      expect(instance.executeQueries.calledOnce).to.eql(true);
      expect(search.calledWith('page', 3)).to.eql(true);
    });

    it('should not execute queries when search query has changed', function() {
      instance.hasSearchQueryStringChanged = sinon.stub().returns(true);

      instance.onPageChange(3, 5);

      expect(instance.executeQueries.calledOnce).to.eql(false);
    });
  });

  describe('onLocationChange', function() {
    beforeEach(function() {
      instance.hasSearchQueryStringChanged = sinon.stub().returns(false);
      instance.getCurrentPageFromSearch = sinon.stub().returns(4);
      instance.executeQueries = sinon.spy();
      instance.locationChange = false;
    });

    it('should do nothing when page did not change', function() {
      instance.pages.current = 4;

      instance.onLocationChange();

      expect(instance.executeQueries.called).to.eql(false);
      expect(instance.locationChange).to.not.eql(true);
    });

    it('should assign new page and mark location and search have changed when page changed ', function() {
      instance.pages.current = 1;
      instance.hasSearchQueryStringChanged = sinon.stub().returns(true);

      instance.onLocationChange();

      expect(instance.executeQueries.called).to.eql(false);
      expect(instance.locationChange).to.eql(true);
      expect(instance.pages.current).to.eql(4);
    });

    it('should execute querries when just page has changed', function() {
      instance.pages.current = 1;

      instance.onLocationChange();

      expect(instance.executeQueries.calledOnce).to.eql(true);
      expect(instance.locationChange).to.not.eql(true);
      expect(instance.pages.current).to.eql(4);
    });
  });

  describe('updateQuery', function() {
    beforeEach(function() {
      instance.areSearchesDifferent = sinon.stub().returns(true);
      instance.resetPage = sinon.spy();
      instance.executeQueries = sinon.spy();
      instance.createQuery = sinon.stub().returns('query-1');
    });

    it('should execute queries when searches are different', function() {
      instance.areSearchesDifferent.returns(true);

      instance.updateQuery();

      expect(instance.executeQueries.calledOnce).to.eql(true);
    });

    it('should not execute queries when searches are different', function() {
      instance.areSearchesDifferent.returns(false);

      instance.updateQuery();

      expect(instance.executeQueries.called).to.eql(false);
    });

    it('should reset page when location has not changed', function() {
      instance.locationChange = false;

      instance.updateQuery();

      expect(instance.resetPage.calledOnce).to.eql(true);
    });

    it('should not reset page when location has changed', function() {
      instance.locationChange = true;

      instance.updateQuery();

      expect(instance.resetPage.called).to.eql(false);
    });

    it('should update last search query string', function() {
      instance.getSearchQueryString = sinon.stub().returns('sss');

      instance.updateQuery();

      expect(instance.lastSearchQueryString).to.eql('sss');
    });

    it('should construct query using default query constructor', function() {
      instance.updateQuery();

      expect(instance.query).to.eql('query-1');
    });

    it('should construct query with custom query constructor', function() {
      instance.buildCustomQuery = sinon.stub().returns('d23');

      instance.updateQuery();

      expect(instance.query).to.eql('d23');
    });
  });

  describe('createQuery', function() {
    var query;

    beforeEach(function() {
      query = 'd';

      searchWidgetUtils.createSearchQueryForSearchWidget.returns(query);
    });

    it('should use searchWidgetUtils to create query', function() {
      var searches = 'searches';

      instance.createQuery(searches);

      expect(
        searchWidgetUtils.createSearchQueryForSearchWidget.calledWith(
          searches,
          instance.arrayTypes,
          instance.variableTypes
        )
      ).to.eql(true);
    });

    it('should return query', function() {
      expect(instance.createQuery()).to.eql(query);
    });
  });

  describe('areSearchesDifferent', function() {
    it('should return true when searches are signifally different', function() {
      expect(
        instance.areSearchesDifferent(
          [createSearch('a', 'a', 'a', 'a')],
          [createSearch('b', 'a', 'a', 'a')]
        )
      ).to.eql(true);
    });

    it('should return false when searches are the same', function() {
      expect(
        instance.areSearchesDifferent(
          [createSearch('a', 'a', 'a', 'a')],
          [createSearch('a', 'a', 'a', 'a')]
        )
      ).to.eql(false);
    });

    it('should return false when searches are basically the same', function() {
      var search = createSearch('a', 'a', 'a', 'a');

      search.d = 1;

      expect(
        instance.areSearchesDifferent(
          [createSearch('a', 'a', 'a', 'a')],
          [search]
        )
      ).to.eql(false);
    });

    function createSearch(type, value, operator, name) {
      return {
        value: {
          value: value
        },
        type: {
          value: {
            value: type
          }
        },
        operator: {
          value: {
            key: operator
          }
        },
        name: {
          value: name
        }
      };
    }
  });

  describe('executeQueries', function() {
    beforeEach(function() {
      instance.onSearchChange = sinon.stub().returns($q.when(5));
      instance.query = 'd';
      instance.blocked = false;
    });

    it('should not execute search when query is not defined', function() {
      instance.query = undefined;
      instance.executeQueries();

      expect(instance.onSearchChange.called).to.eql(false);
    });

    it('should not execute search when blocked', function() {
      instance.locationChange = true;
      instance.executeQueries();

      expect(instance.locationChange).to.eql(false);
    });

    it('should set pages total', function() {
      instance.executeQueries();

      $rootScope.$digest();

      expect(instance.pages.total).to.eql(5);
    });

    it('should execute search with query and pages', function() {
      instance.executeQueries();

      expect(
        instance.onSearchChange.calledWith({
          query: instance.query,
          pages: instance.pages
        })
      ).to.eql(true);
    });
  });
});
