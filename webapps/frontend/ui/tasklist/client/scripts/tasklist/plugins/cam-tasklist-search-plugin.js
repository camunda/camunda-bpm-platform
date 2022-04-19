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
var fs = require('fs');

var template = require('./cam-tasklist-search-plugin.html')();
var searchConfigJSON = require('./cam-tasklist-search-plugin-config.json');

var angular = require('../../../../../../camunda-commons-ui/vendor/angular');
var moment = require('../../../../../../camunda-commons-ui/vendor/moment');

var expressionsRegex = /^[\s]*([#$]){/;
var simpleDateExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(|\.[0-9]{0,4})$/;

var searchConfig = searchConfigJSON;

var parseValue = function(value, enforceString) {
  if (enforceString) {
    return '' + value;
  }
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
};

var sanitizeValue = function(value, operator, search) {
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
  } else if (operator === 'In') {
    return value.split(',');
  } else if (search.allowDates && simpleDateExp.test(value)) {
    return moment(value, moment.ISO_8601).format('YYYY-MM-DDTHH:mm:ss.SSSZZ');
  }
  return value;
};

var getQueryValueBySearch = function(search) {
  if (search.basic) {
    return true;
  }
  return sanitizeValue(
    parseValue(search.value.value, search.enforceString),
    search.operator.value.key,
    search
  );
};

var sanitizeProperty = function(search, type, operator, value) {
  var out = type;
  if (['Like', 'Before', 'After'].indexOf(operator) !== -1) {
    out += operator;
  }
  if (
    expressionsRegex.test(value) &&
    [
      'assignee',
      'owner',
      'candidateGroup',
      'candidateUser',
      'involvedUser',
      'processInstanceBusinessKey'
    ].indexOf(type) !== -1
  ) {
    out += 'Expression';
  }
  if (type === 'priority' && operator !== 'eq') {
    out = operator + 'Priority';
  }
  return out;
};

var Controller = [
  '$scope',
  '$translate',
  '$location',
  function($scope, $translate, $location) {
    $scope.searches = [];
    $scope.translations = {};
    $scope.matchAny = $location.search()['searchOrQuery'] || false;

    $scope.translations = searchConfig.tooltips;
    $scope.types = searchConfig.types;
    $scope.operators = searchConfig.operators;

    var searchData = $scope.tasklistData.newChild($scope);
    $scope.$watch(
      '[searches, matchAny]',
      function() {
        var baseQuery = {};
        var tempQuery;

        if ($scope.matchAny === true) {
          baseQuery.orQueries = [{}];
          tempQuery = baseQuery.orQueries[0];
          tempQuery.processVariables = [];
          tempQuery.taskVariables = [];
          tempQuery.caseInstanceVariables = [];
        } else {
          baseQuery.processVariables = [];
          baseQuery.taskVariables = [];
          baseQuery.caseInstanceVariables = [];
          tempQuery = baseQuery;
        }

        angular.forEach($scope.searches, function(search) {
          if (typeof tempQuery[search.type.value.key] === 'object') {
            tempQuery[search.type.value.key].push({
              name:
                typeof search.name.value === 'object'
                  ? search.name.value.key
                  : search.name.value,
              operator: search.operator.value.key,
              value: getQueryValueBySearch(search)
            });
          } else {
            tempQuery[
              sanitizeProperty(
                search,
                search.type.value.key,
                search.operator.value.key,
                search.value.value
              )
            ] = getQueryValueBySearch(search);
          }
        });

        if ($scope.matchAny === false) {
          delete baseQuery.orQueries;
        }

        searchData.set('searchQuery', baseQuery);
      },
      true
    );

    searchData.observe('currentFilter', function(filter) {
      angular.forEach($scope.types, function(ea) {
        ea.potentialNames = [];
        for (
          var i = 0;
          i <
            (filter &&
              filter.properties &&
              filter.properties.variables &&
              filter.properties.variables.length) || 0;
          i++
        ) {
          var v = filter.properties.variables[i];
          ea.potentialNames.push({
            key: v.name,
            value: v.label + ' (' + v.name + ')'
          });
        }
      });

      angular.forEach($scope.searches, function(ea) {
        ea.potentialNames = $scope.types.filter(function(type) {
          return type.id.key === ea.type.value.key;
        })[0].potentialNames;
      });
    });

    searchData.observe('taskList', function(taskList) {
      $scope.totalItems = taskList.count;
    });
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.list', {
    id: 'task-search',
    template: template,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
