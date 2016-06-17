'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-search-plugin.html', 'utf8');
var searchConfigJSON = fs.readFileSync(__dirname + '/cam-tasklist-search-plugin-config.json', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var expressionsRegex = /^[\s]*(\#|\$)\{/;

var searchConfig = JSON.parse(searchConfigJSON);

var parseValue = function(value, enforceString) {
  if(enforceString) {
    return '' + value;
  }
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
};

var sanitizeValue = function(value, operator) {
  if(operator === 'Like' || operator === 'like') {
    return '%'+value+'%';
  } else if(operator == 'in') {
    return value.split(',');
  }
  return value;
};

var getQueryValueBySearch = function(search) {
  if (search.basic) {
    return true;
  }
  return sanitizeValue(parseValue(search.value.value, search.enforceString), search.operator.value.key);
};

var sanitizeProperty = function(search, type, operator, value) {
  var out = type;
  if(['Like', 'Before', 'After'].indexOf(operator) !== -1) {
    out += operator;
  }
  if(expressionsRegex.test(value) &&
       ['assignee', 'owner', 'candidateGroup', 'candidateUser', 'involvedUser'].indexOf(type) !== -1) {
    out += 'Expression';
  }
  if(type === 'priority' && operator !== 'eq') {
    out = operator + 'Priority';
  }
  return out;
};

var Controller = [
  '$scope',
  '$translate',
  function(
    $scope,
    $translate
  ) {

    $scope.searches = [];
    $scope.translations = {};

    angular.forEach(searchConfig.tooltips, function(value, key) {
      $scope.translations[key] = $translate.instant(value);
    });

    $scope.types = searchConfig.types.map(function(el) {
      el.id.value = $translate.instant(el.id.value);
      if(el.operators) {
        el.operators = el.operators.map(function(op) {
          op.value = $translate.instant(op.value);
          return op;
        });
      }
      return el;
    });

    $scope.operators = searchConfig.operators;
    angular.forEach($scope.operators.date, function(el) {
      el.value = $translate.instant(el.value);
    });

    var searchData = $scope.tasklistData.newChild($scope);
    $scope.$watch('searches', function() {
      var query = {};

      query.processVariables = [];
      query.taskVariables = [];
      query.caseInstanceVariables = [];

      angular.forEach($scope.searches, function(search) {
        if(typeof query[search.type.value.key] === 'object') {
          query[search.type.value.key].push({
            name: typeof search.name.value === 'object' ? search.name.value.key : search.name.value,
            operator: search.operator.value.key,
            value: getQueryValueBySearch(search)
          });
        } else {
          query[sanitizeProperty(search, search.type.value.key, search.operator.value.key, search.value.value)] = getQueryValueBySearch(search);
        }
      });

      searchData.set('searchQuery', query);
    }, true);

    searchData.observe('currentFilter', function(filter) {
      angular.forEach($scope.types, function(ea) {
        ea.potentialNames = [];
        for(var i = 0; i < (filter.properties.variables && filter.properties.variables.length) || 0; i++) {
          var v = filter.properties.variables[i];
          ea.potentialNames.push({
            key: v.name,
            value: v.label+' ('+v.name+')'
          });
        }
      });

      angular.forEach($scope.searches, function(ea) {
        ea.potentialNames = $scope.types.filter(function(type) {
          return type.id.key === ea.type.value.key;
        })[0].potentialNames;
      });
    });

  }];

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
