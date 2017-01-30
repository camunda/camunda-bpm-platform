'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope', 'exposeScopeProperties',
  ExternalTasksTabController
];

module.exports.ExternalTasksTabController = ExternalTasksTabController;

function ExternalTasksTabController($scope, exposeScopeProperties) {
  exposeScopeProperties($scope, this, ['onLoad']);

  var processData = $scope.processData.newChild($scope);

  processData.observe('filter', this.onFilterChanged.bind(this));
}

ExternalTasksTabController.prototype.onFilterChanged = function(filter) {
  if (this.isFilterChanged(filter)) {
    this.filter = filter;

    if (this.pages) {
      this.loadTasks();
    }
  }
};

ExternalTasksTabController.prototype.isFilterChanged = function(filter) {
  var lastActivity = getActivityIdFromFilter(this.filter);
  var currentActivity = getActivityIdFromFilter(filter);

  return !this.filter || lastActivity !== currentActivity;
};

ExternalTasksTabController.prototype.onPaginationChange = function(pages) {
  this.pages = pages;

  if (this.filter) {
    this.loadTasks();
  }
};

ExternalTasksTabController.prototype.loadTasks = function() {
  this.loadingState = 'LOADING';

  this.onLoad({
    pages: angular.copy(this.pages), //just a defensive copy
    params: this.getFilterParams()
  })
  .then((function(data) {
    this.total = data.count;

    if (!data.list) {
      this.loadingState = 'EMPTY';
    } else {
      this.loadingState = 'LOADED';
    }
  }).bind(this));
};

ExternalTasksTabController.prototype.getFilterParams = function() {
  var activityId = getActivityIdFromFilter(this.filter);

  if (!activityId) {
    return {};
  }

  return {
    activityId: activityId
  };
};

function getActivityIdFromFilter(filter) {
  if (!filter || !filter.activityIds || !filter.activityIds.length) {
    return null;
  }

  return filter.activityIds[0];
}
