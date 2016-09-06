'use strict';

var angular = require('angular');
var includes = require('./includes');

module.exports = createSearchQueryForSearchWidget;

/**
 * Function that creates query parameters based on search pills
 *
 * @param searches list of search pills
 * @param arrayTypes list of types that should be arrays
 * @returns {*}
 */
function createSearchQueryForSearchWidget(searches, arrayTypes) {
  searches = angular.isArray(searches) ? searches : [];
  arrayTypes = angular.isArray(arrayTypes) ? arrayTypes : [];

  return searches.reduce(addSearchToQuery.bind(null, arrayTypes), {});
}

function addSearchToQuery(arrayTypes, query, search) {
  var type = getSearchType(search);
  var value = getSearchValue(search);

  if (includes(arrayTypes, type)) {
    query[type] = angular.isArray(query[type]) ? query[type].concat([value]) : [value];
  } else {
    query[type] = value;
  }

  return query;
}

function getSearchType(search) {
  var type = search.type.value.key;
  var op = search.operator.value.key;

  if (isDateType(type)) {
    type = type.slice(0, -4);
  }

  if (isOperatorAppendable(op)) {
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
