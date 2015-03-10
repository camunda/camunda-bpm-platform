define([
  'angular',
  'text!./cam-tasklist-search.html',
  'text!./cam-tasklist-search-config.json'
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
    return value;
  };

  var sanitizeValue = function(value, operator) {
    if(operator === 'like') {
      return '%'+value+'%';
    }
    return value;
  };

  return [function() {

    return {
      restrict: 'A',

      scope: {
        tasklistData: '='
      },

      controller: ['$scope', '$translate', function($scope, $translate) {

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

          searchData.set("searchQuery", query);
        }, true);

      }],

      template: template
    };
  }];
});
