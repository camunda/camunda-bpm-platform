define([
  'angular',
  'text!./cam-tasklist-search-plugin.html',
  'text!./cam-tasklist-search-plugin-config.json'
], function(
  angular,
  template,
  searchConfigJSON
) {
  'use strict';

  var parseValue = function(value) {
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
    if(operator === 'like') {
      return '%'+value+'%';
    }
    return value;
  };

  var Controller = [
   '$scope',
   '$translate',
  function (
    $scope,
    $translate
  ) {

    var searchConfig = JSON.parse(searchConfigJSON);

    $scope.searches = [];
    $scope.translations = {};

    angular.forEach(searchConfig.tooltips, function(value, key) {
      $scope.translations[key] = $translate.instant(value);
    });

    $scope.types = searchConfig.types.map(function(el) {
      return {
        id: {
          key: el.key,
          value: $translate.instant(el.value)
        },
        extended: true,
        allowDates: true
      };
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
        query[search.type.value.key].push({
          name: search.name.value,
          operator: search.operator.value.key,
          value: sanitizeValue(parseValue(search.value.value), search.operator.value.key)
        });
      });

      searchData.set('searchQuery', query);
    }, true);

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

  return Configuration;

});
