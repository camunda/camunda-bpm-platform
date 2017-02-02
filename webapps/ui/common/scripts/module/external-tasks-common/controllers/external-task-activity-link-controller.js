'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope', 'exposeScopeProperties', 'search', '$location',
  'params', 'searchWidgetUtils',
  ExternalTaskActivityLink
];

function ExternalTaskActivityLink($scope, exposeScopeProperties, search,
   $location, params, searchWidgetUtils) {
  exposeScopeProperties($scope, this, ['activityId', 'bpmnElements', 'searchQueryType']);

  this.search = search;
  this.params = params;
  this.path = $location.path();
  this.searchWidgetUtils = searchWidgetUtils;
}

ExternalTaskActivityLink.prototype.getLink = function() {
  //search returns object that when modified changes query parameters
  //which is not desired here, hence it is needed to actually copy this object.
  var params = angular.copy(this.search());
  var searchQuery = JSON.parse(params.searchQuery || '[]');

  if (this.searchQueryType) {
    params.searchQuery = JSON.stringify(
      this.searchWidgetUtils.replaceActivitiesInSearchQuery(
        searchQuery,
        this.searchQueryType,
        [this.activityId]
      )
    );
  } else {
    params.activityIds = this.activityId;
  }

  return '#' + this.path + '?' + this.params(params);
};

ExternalTaskActivityLink.prototype.getActivityName = function() {
  var activityId = this.activityId;

  if (this.bpmnElements[activityId] && this.bpmnElements[activityId].name) {
    return this.bpmnElements[activityId].name;
  }

  return activityId;
};
