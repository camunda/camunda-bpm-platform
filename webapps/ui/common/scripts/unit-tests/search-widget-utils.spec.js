'use strict';

var expect = require('chai').expect;
var searchWidgetUtils = require('../util/search-widget-utils');

describe('common/utils searchWidgetUtils', function() {
  var searchType;

  beforeEach(function() {
    searchType = 'search-type';
  });

  describe('getSearchQueryForSearchType', function() {
    it('should provide query for values', function() {
      var values = ['a00a12', 'bb0023'];
      var query = searchWidgetUtils.getSearchQueryForSearchType(searchType, values);

      expect(query).to.contain(values[0]);
      expect(query).to.contain(values[1]);
      expect(query).to.contain(searchType);
    });
  });

  describe('encodeQuery', function() {
    it('should encode #', function() {
      expect(searchWidgetUtils.encodeQuery('#')).to.eql('%23');
    });

    it('should use encode query', function() {
      expect(searchWidgetUtils.encodeQuery('[')).to.eql('%5B');
    });
  });

  describe('shouldUpdateFilter', function() {
    var newFilter;
    var currentFilter;

    beforeEach(function() {
      newFilter = {
        a: 1,
        b: 22,
        c: 43
      };

      currentFilter = {
        a: 40003,
        b: 22,
        d: 800,
        f: 45
      };
    });

    it('should return true when at least one of white-listed properties changed', function() {
      expect(
        searchWidgetUtils.shouldUpdateFilter(newFilter, currentFilter, ['a', 'b'])
      ).to.eql(true);
    });

    it('should return false when none of white-listed properties changed', function() {
      expect(
        searchWidgetUtils.shouldUpdateFilter(newFilter, currentFilter, ['b'])
      ).to.eql(false);
    });

    it('should ignore undefined properties', function() {
      newFilter.x = undefined;

      expect(
        searchWidgetUtils.shouldUpdateFilter(newFilter, currentFilter, ['x', 'b'])
      ).to.eql(false);
    });
  });

  describe('getActivityIdsFromUrlParams', function() {
    var params;

    beforeEach(function() {
      params = {
        searchQuery: JSON.stringify([
          {
            type: searchType,
            value: 'x'
          }
        ])
      };
    });

    it('should extract values from given params', function() {
      expect(searchWidgetUtils.getActivityIdsFromUrlParams(searchType, params)).to.eql(['x']);
    });

    it('should by default use empty list for searches', function() {
      expect(searchWidgetUtils.getActivityIdsFromUrlParams(searchType, {})).to.eql([]);
    });
  });

  describe('updateSearchValuesForTypeInCtrlMode', function() {
    var searches;
    var values;
    var newSearches;

    beforeEach(function() {
      values = [3, 4];
      searches = [
        {
          type: searchType,
          value: 3
        },
        {
          type: searchType,
          value: 5
        }
      ];

      newSearches = searchWidgetUtils.updateSearchValuesForTypeInCtrlMode(searches, searchType, values);
    });

    it('should add new values and remove repeating ones', function() {
      expect(
        newSearches.map(function(search) {
          return search.value;
        })
      ).to.eql([5, 4]);
    });
  });

  describe('replaceActivitiesInSearchQuery', function() {
    var searches;
    var values;
    var newSearches;

    beforeEach(function() {
      values = [1, 4];
      searches = [
        {
          type: searchType + '_not',
          value: 50
        },
        {
          type: searchType,
          value: 3
        },
        {
          type: searchType,
          value: 5
        }
      ];

      newSearches = searchWidgetUtils.replaceActivitiesInSearchQuery(searches, searchType, values);
    });

    it('should replace values of given type', function() {
      var valuesForType = newSearches
        .filter(function(search) {
          return search.type === searchType;
        })
        .map(function(search) {
          return search.value;
        });

      expect(valuesForType).to.eql([1, 4]);
    });

    it('should do nothing with values of other types', function() {
      var valuesForType = newSearches
        .filter(function(search) {
          return search.type !== searchType;
        })
        .map(function(search) {
          return search.value;
        });

      expect(valuesForType).to.eql([50]);
    });
  });

  describe('createSearchQueryForSearchWidget', function() {
    var searches;
    var arrayTypes;
    var variableTypes;
    var query;

    beforeEach(function() {
      searches = [
        createMockSearch('a', 'eq', '1'),
        createMockSearch('b', 'Like', 'ded'),
        createMockSearch('c', 'eq', '2', true),
        createMockSearch('arr1', 'eq', '1'),
        createMockSearch('arr1', 'eq', '2'),
        createMockSearch('var1', 'eq', '20', false, 'x'),
        createMockSearch('var1', 'Like', '22', false, 'y'),
        createMockSearch('dDate', 'After', 'd')
      ];

      arrayTypes = ['arr1'];
      variableTypes = ['var1'];

      query = searchWidgetUtils.createSearchQueryForSearchWidget(searches, arrayTypes, variableTypes);
    });

    it('should contain simple query properties with eq operator', function() {
      expect(query.a).to.equal('1');
    });

    it('should contain properties with Like operator', function() {
      expect(query.bLike).to.eql('%ded%');
    });

    it('should contain basic properties', function() {
      expect(query.c).to.eql(true);
    });

    it('should contain array properties', function() {
      expect(query.arr1).to.eql(['1', '2']);
    });

    it('should contain variable properties', function() {
      expect(query.var1).to.eql([
        {
          name: 'x',
          operator: 'eq',
          value: 20
        },
        {
          name: 'y',
          operator: 'Like',
          value: '%22%'
        }
      ]);
    });

    it('should contain date properties', function() {
      expect(query.dAfter).to.eql('d');
    });

    function createMockSearch(type, operator, value, basic, name) {
      return {
        type: {
          value: {
            key: type
          }
        },
        operator: {
          value: {
            key: operator
          }
        },
        basic: basic,
        value: {
          value: value
        },
        name: {
          value: name
        }
      };
    }
  });
});
