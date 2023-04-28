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

var angular = require('camunda-commons-ui/vendor/angular');
var moment = require('camunda-commons-ui/vendor/moment');
var includes = require('./includes');

module.exports = {
  getSearchQueryForSearchType: getSearchQueryForSearchType,
  getActivityIdsFromUrlParams: getActivityIdsFromUrlParams,
  replaceActivitiesInSearchQuery: replaceActivitiesInSearchQuery,
  createSearchQueryForSearchWidget: createSearchQueryForSearchWidget,
  shouldUpdateFilter: shouldUpdateFilter,
  createSearchesForActivityIds: createSearchesForActivityIds,
  encodeQuery: encodeQuery,
  updateSearchValuesForTypeInCtrlMode: updateSearchValuesForTypeInCtrlMode
};

/**
 * Creates url part for search query with values for given search type
 *
 * @param searchType
 * @param values list of ids or single id
 */
function getSearchQueryForSearchType(searchType, values) {
  values = [].concat(values); //convert single id to list of ids or in case of array do nothing

  var value = JSON.stringify(createSearchesForActivityIds(searchType, values));

  return encodeQuery('searchQuery=' + value);
}

function encodeQuery(query) {
  return encodeURI(query).replace(/#/g, '%23');
}

/**
 * Function that returns true when filter has meaningful changes.
 *
 * @param newFilter
 * @param currentFilter
 * @param whiteList list of properties that should be ignored while comparing object, by default []
 * @returns {boolean}
 */
function shouldUpdateFilter(newFilter, currentFilter, whiteList) {
  whiteList = angular.isArray(whiteList) ? whiteList : [];

  return !angular.equals(
    prepareObjectForComparing(newFilter, whiteList),
    prepareObjectForComparing(currentFilter, whiteList)
  );
}

function prepareObjectForComparing(obj, whiteList) {
  if (!angular.isObject(obj)) {
    return obj;
  }

  return stripUndefinedFromObject(stripProperties(obj, whiteList));
}

function stripProperties(obj, whiteList) {
  return Object.keys(obj).reduce(function(newObj, key) {
    var value = obj[key];

    if (includes(whiteList, key)) {
      newObj[key] = value;
    }

    return newObj;
  }, {});
}

function stripUndefinedFromObject(obj) {
  return Object.keys(obj).reduce(function(newObj, key) {
    var value = obj[key];

    if (value != null) {
      newObj[key] = value;
    }

    return newObj;
  }, {});
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
 * Updates values in search query for given search type when control is pressed
 *
 * @param searches
 * @param searchType
 * @param values
 * @returns {Array.<T>|string}
 */
function updateSearchValuesForTypeInCtrlMode(searches, searchType, values) {
  var newSearches = removeDoubledSearches(searches, values, searchType);
  var newValues = removeDoubleValues(values, searches, searchType);

  return newSearches.concat(
    createSearchesForActivityIds(searchType, newValues)
  );
}

function removeDoubledSearches(searches, values, searchType) {
  return searches.filter(function(search) {
    return search.type !== searchType || !includes(values, search.value);
  });
}

function removeDoubleValues(values, searches, searchType) {
  var searchesValues = searches
    .filter(function(search) {
      return search.type === searchType;
    })
    .map(function(search) {
      return search.value;
    });

  return values.filter(function(value) {
    return !includes(searchesValues, value);
  });
}

/**
 * Updates search widget with selected activity ids (usually from filter)
 *
 * @param search service that updates location search
 * @param searchType type of activity search pill
 * @param selectedActivityIds list of ids for selected activities
 */
function replaceActivitiesInSearchQuery(
  searches,
  searchType,
  selectedActivityIds
) {
  return removeActivitySearches(searchType, searches).concat(
    createSearchesForActivityIds(searchType, selectedActivityIds)
  );
}

function removeActivitySearches(searchType, searches) {
  return searches.filter(function(search) {
    return search.type !== searchType;
  });
}

function createSearchesForActivityIds(searchType, activityIds) {
  return activityIds.map(createActivitySearch.bind(null, searchType));
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
  variableTypes = angular.isArray(variableTypes)
    ? variableTypes
    : ['variables'];

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

  return sanitizeValue(
    search.value.key,
    search.value.value,
    search.operator.value.key
  );
}

var simpleDateExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(|\.[0-9]{0,4})$/;
function sanitizeValue(key, value, operator) {
  // Regex for '\_' and '\%' epxressions
  var specialWildCardCharExp = /(\\%)|(\\_)/g;
  // Regex for '_' and '%' special characters
  var wildCardExp = /(%)|(_)/;
  if (
    (operator.toLowerCase() === 'like' ||
      operator.toLowerCase() === 'notlike') &&
    !wildCardExp.test(value.replace(specialWildCardCharExp, ''))
  ) {
    return '%' + value + '%';
  } else if (simpleDateExp.test(value)) {
    return moment(value, moment.ISO_8601).format('YYYY-MM-DDTHH:mm:ss.SSSZZ');
  }
  return key ? key : value;
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
  if (value.indexOf("'") === 0 && value.lastIndexOf("'") === value.length - 1) {
    return value.substr(1, value.length - 2);
  }
  return value;
}

function createVariableValue(search, value) {
  return {
    name: search.name.value,
    operator: search.operator.value.key,
    value: parseValue(value)
  };
}

function appendNewValueToArrayType(query, type, value) {
  return angular.isArray(query[type]) ? query[type].concat([value]) : [value];
}
