'use strict';

var angular = require('angular');
var includes = require('./includes');

module.exports = {
  getActivityIdsBasedOnInstances: getActivityIdsBasedOnInstances,
  getActivityIdsFromUrlParams: getActivityIdsFromUrlParams,
  replaceActivitiesInSearchQuery: replaceActivitiesInSearchQuery,
  createSearchQueryForSearchWidget: createSearchQueryForSearchWidget
};

/**
 * Gets list of activity ids based on instances ids
 *
 * @param instanceIdToInstanceMap
 * @param ids
 * @returns {Array}
 */
function getActivityIdsBasedOnInstances(instanceIdToInstanceMap, ids) {
  ids = angular.isArray(ids) ? ids : [];

  if (!instanceIdToInstanceMap) {
    return [];
  }

  return ids
    .filter(function(id) {
      return instanceIdToInstanceMap[id];
    })
    .map(function(id) {
      return instanceIdToInstanceMap[id].activityId;
    });
}

/**
 * Extracts activity ids from searchQuery param
 *
 * @param searchType type of pill with activity ids
 * @param params url params
 * @returns {*}
 */
function getActivityIdsFromUrlParams(searchType, params) {
  var searches = JSON.parse(params.searchQuery || '[]');

  return getActivityIdsFromSearches(searchType, searches);
}

function getActivityIdsFromSearches(searchType, searches) {
  return searches
    .filter(function(search) {
      return search.type === searchType;
    })
    .map(function(search) {
      return search.value;
    });
}

/**
 * Updates search widget with selected activity ids (usually from filter)
 *
 * @param search service that updates location search
 * @param searchType type of activity search pill
 * @param selectedActivityIds list of ids for selected activities
 */
function replaceActivitiesInSearchQuery(search, searchType, selectedActivityIds) {
  selectedActivityIds = angular.isArray(selectedActivityIds) ? selectedActivityIds : [];

  var urlParams = search();

  //when there is no searchQuery present and there is no ids to add to searchQuery don't change anything
  if (!urlParams.searchQuery && !selectedActivityIds.length) {
    return null;
  }

  return constructNewSearchQuery(urlParams, searchType, selectedActivityIds);
}

function constructNewSearchQuery(urlParams, searchType, selectedActivityIds) {
  var searches = JSON.parse(urlParams.searchQuery || '[]');

  searches = removeActivitySearches(searchType, searches)
    .concat(createSearchesForActivityIds(searchType, selectedActivityIds));

  return JSON.stringify(searches);
}

function removeActivitySearches(searchType, searches) {
  return searches.filter(function(search) {
    return search.type !== searchType;
  });
}

function createSearchesForActivityIds(searchType, activityIds) {
  return activityIds.map(
    createActivitySearch.bind(null, searchType)
  );
}

function createActivitySearch(searchType, value) {
  return {
    type: searchType,
    operator: 'eq',
    value: value
  };
}

/**
 * Function that creates query parameters based on search pills
 *
 * @param searches list of search pills
 * @param arrayTypes list of types that should be arrays
 * @param variableTypes list of types that should be treated as variables,
 *        by default it is singleton with variable type
 * @returns {*}
 */
function createSearchQueryForSearchWidget(searches, arrayTypes, variableTypes) {
  searches = angular.isArray(searches) ? searches : [];
  arrayTypes = angular.isArray(arrayTypes) ? arrayTypes : [];
  variableTypes = angular.isArray(variableTypes) ? variableTypes : ['variables'];

  //all variable types are also array types
  arrayTypes = arrayTypes.concat(variableTypes);

  return searches.reduce(
    addSearchToQuery.bind(null, arrayTypes, variableTypes),
    {}
  );
}

function addSearchToQuery(arrayTypes, variableTypes, query, search) {
  var type = getSearchType(search, variableTypes);
  var value = getSearchValue(search, type, variableTypes);

  if (includes(variableTypes, type)) {
    value = createVariableValue(search, value);
  }

  if (includes(arrayTypes, type)) {
    query[type] = appendNewValueToArrayType(query, type, value);
  } else {
    query[type] = value;
  }

  return query;
}

function getSearchType(search, variableTypes) {
  var type = search.type.value.key;
  var op = search.operator.value.key;

  if (isDateType(type)) {
    type = type.slice(0, -4);
  }

  if (isOperatorAppendable(op) && !includes(variableTypes, type)) {
    type += op;
  }

  return type;
}

function isDateType(type) {
  return type.indexOf('Date') !== -1;
}

function isOperatorAppendable(op) {
  return includes(['After', 'Before', 'Like'], op);
}

function getSearchValue(search) {
  if (search.basic) {
    return true;
  }

  return sanitizeValue(parseValue(search.value.value), search.operator.value.key);
}

function sanitizeValue(value, operator) {
  if (includes(['like', 'Like'], operator)) {
    return '%'+value+'%';
  }
  return value;
}

function parseValue(value) {
  if (!isNaN(value) && value.trim() !== '') {
    // value must be transformed to number
    return +value;
  }
  if (value === 'true') {
    return true;
  }
  if (value === 'false') {
    return false;
  }
  if (value === 'NULL') {
    return null;
  }
  if(value.indexOf('\'') === 0 && value.lastIndexOf('\'') === value.length - 1) {
    return value.substr(1, value.length - 2);
  }
  return value;
}

function createVariableValue(search, value) {
  return {
    name: search.name.value,
    operator: search.operator.value.key,
    value: value
  };
}

function appendNewValueToArrayType(query, type, value) {
  return angular.isArray(query[type]) ? query[type].concat([value]) : [value];
}
