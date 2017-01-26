'use strict';

module.exports = [
  '$scope', 'exposeScopeProperties', 'externalTasks',
  ProcessInstanceRuntimeExternalTasksController
];

function ProcessInstanceRuntimeExternalTasksController($scope, exposeScopeProperties, externalTasks) {
  this.externalTasks = externalTasks;

  exposeScopeProperties($scope, this, ['processInstance']);

  var processData = $scope.processData.newChild($scope);

  processData.observe('filter', this.onFilterChanged.bind(this));
}

ProcessInstanceRuntimeExternalTasksController.prototype.onFilterChanged = function(filter) {
  if (this.isFilterChanged(filter)) {
    this.filter = filter;

    if (this.pages) {
      this.loadTasks();
    }
  }
};

ProcessInstanceRuntimeExternalTasksController.prototype.isFilterChanged = function(filter) {
  var lastActivity = getActivityIdFromFilter(this.filter);
  var currentActivity = getActivityIdFromFilter(filter);

  return !this.filter || lastActivity !== currentActivity;
};

ProcessInstanceRuntimeExternalTasksController.prototype.onPaginationChange = function(pages) {
  this.pages = pages;

  if (this.filter) {
    this.loadTasks();
  }
};

ProcessInstanceRuntimeExternalTasksController.prototype.loadTasks = function() {
  this.loadingState = 'LOADING';

  this.externalTasks
    .getActiveExternalTasksForProcess(
      this.processInstance.id,
      this.pages,
      this.getFilterParams()
    )
    .then((function(data) {
      this.tasks = data.tasks;
      this.total = data.count;

      if (!this.tasks) {
        this.loadingState = 'EMPTY';
      } else {
        this.loadingState = 'LOADED';
      }
    }).bind(this));
};

ProcessInstanceRuntimeExternalTasksController.prototype.getFilterParams = function() {
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
