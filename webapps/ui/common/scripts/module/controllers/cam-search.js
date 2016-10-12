var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['$scope', 'exposeScopeProperties', 'searchWidgetUtils', CamSearch];

function CamSearch($scope, exposeScopeProperties, searchWidgetUtils) {
  this.searchWidgetUtils = searchWidgetUtils;

  exposeScopeProperties($scope, this, [
    'config',
    'onQueryChange',
    'arrayTypes',
    'variableTypes'
  ]);

  this.init($scope);
}

CamSearch.prototype.init = function($scope) {
  this.arrayTypes = angular.isArray(this.arrayTypes) ? this.arrayTypes : [];
  this.variableTypes = angular.isArray(this.variableTypes) ? this.variableTypes : [];

  $scope.$watch(
    'config.searches',
    this.updateQuery.bind(this),
    true
  );
};

CamSearch.prototype.updateQuery = function(newValue, oldValue) {
  if (!angular.equals(newValue, oldValue)) {
    var query = this.createQuery(newValue);

    this.onQueryChange({
      query: query
    });
  }
};

CamSearch.prototype.createQuery = function(searches) {
  return this.searchWidgetUtils.createSearchQueryForSearchWidget(searches, this.arrayTypes, this.variableTypes);
};
