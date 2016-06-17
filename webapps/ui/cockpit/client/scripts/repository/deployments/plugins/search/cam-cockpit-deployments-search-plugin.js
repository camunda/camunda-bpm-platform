'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-deployments-search-plugin.html', 'utf8');
var searchConfigJSON = fs.readFileSync(__dirname + '/cam-cockpit-deployments-search-plugin-config.json', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var searchConfig = JSON.parse(searchConfigJSON);

var Controller = [
  '$scope',
  function(
    $scope
  ) {

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
      return value;
    };

    var sanitizeValue = function(value, operator) {
      if(operator === 'Like' || operator === 'like') {
        return '%'+value+'%';
      }
      return value;
    };

    var getQueryParamBySearch = function(search) {
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
      if (search.basic) {
        return true;
      }
      return sanitizeValue(parseValue(search.value.value, search.enforceString), search.operator.value.key);
    };

    var addSearchToQuery = function(query, search) {
      var type = getQueryParamBySearch(search);
      query[type] = getQueryValueBySearch(search);
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

  ViewsProvider.registerDefaultView('cockpit.repository.deployments.list', {
    id: 'deployments-search',
    template: template,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
