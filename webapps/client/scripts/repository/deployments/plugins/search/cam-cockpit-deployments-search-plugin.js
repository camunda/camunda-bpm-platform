define([
  'angular',
  'text!./cam-cockpit-deployments-search-plugin.html',
  'text!./cam-cockpit-deployments-search-plugin-config.json'
], function(
  angular,
  template,
  searchConfigJSON
) {
  'use strict';

  var searchConfig = JSON.parse(searchConfigJSON);

  var Controller = [
   '$scope',
  function (
    $scope
  ) {

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
      return value;
    };

    var sanitizeValue = function(value, operator) {
      if(operator === 'Like' || operator === 'like') {
        return '%'+value+'%';
      }
      return value;
    };

    var getQueryParamBySearch = function (search) {
      var param = search.type.value.key;
      var op = search.operator.value.key;

      if ([ 'Like', 'like' ].indexOf(op) !== -1) {
        param += op;
      }
      else if ([ 'Before', 'before', 'After', 'after' ].indexOf(op) !== -1) {
        param = op.toLowerCase();
      }

      return param;
    };

    var getQueryValueBySearch = function(search) {
      return sanitizeValue(parseValue(search.value.value), search.operator.value.key)
    };

    var addSearchToQuery = function(query, search) {
      var type = getQueryParamBySearch(search);
      var value = getQueryValueBySearch(search);
      query[type] = value;
    };

    $scope.translations = searchConfig.tooltips;
    $scope.types = searchConfig.types;
    $scope.operators = searchConfig.operators;

    var searchData = $scope.deploymentsData.newChild($scope);

    $scope.$watch('searches', function(searches) {
      var query = {};
      angular.forEach(searches, function(search) {
        addSearchToQuery(query, search);
      });

      searchData.set('deploymentsSearchQuery', query);
    }, true);

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cam.cockpit.repository.deployments.list', {
      id: 'deployments-search',
      template: template,
      controller: Controller,
      priority: 100
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
