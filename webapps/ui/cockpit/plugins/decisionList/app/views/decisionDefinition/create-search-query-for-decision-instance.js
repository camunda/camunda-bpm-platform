'use strict';

var angular = require('angular');

module.exports = createSearchQueryForDecisionInstance;

function createSearchQueryForDecisionInstance(searches) {
  searches = searches || [];

  return searches.reduce(addSearchToQuery, {});
}

function addSearchToQuery(query, search) {
  var type = getSearchType(search);
  var value = getSearchValue(search);

  if (isArraySearchType(type)) {
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
  return includes(['After', 'Before'], op);
}

function getSearchValue(search) {
  if (search.basic) {
    return true;
  }

  return parseValue(search.value.value);
}

function parseValue(value) {
  if(!isNaN(value) && value.trim() !== '') {
    // value must be transformed to number
    return +value;
  }
  if(value === 'true') {
    return true;
  }
  if(value === 'false') {
    return false;
  }
  if(value === 'NULL') {
    return null;
  }
  if(value.indexOf('\'') === 0 && value.lastIndexOf('\'') === value.length - 1) {
    return value.substr(1, value.length - 2);
  }
  return value;
}

function isArraySearchType(type) {
  return includes(['activityIdIn', 'activityInstanceIdIn'], type);
}

function includes(array, value) {
  return array.indexOf(value) !== -1;
}
