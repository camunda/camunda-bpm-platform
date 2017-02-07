'use strict';

module.exports = [
  '$scope', 'exposeScopeProperties', 'externalTasks', 'observeBpmnElements',
  ProcessInstanceRuntimeTab
];

function ProcessInstanceRuntimeTab($scope, exposeScopeProperties, externalTasks, observeBpmnElements) {
  exposeScopeProperties($scope, this, ['processInstance', 'processData']);
  this.externalTasks = externalTasks;

  observeBpmnElements($scope, this);
}

ProcessInstanceRuntimeTab.prototype.onLoad = function(pages, activityIds) {
  return this.externalTasks.getActiveExternalTasksForProcess(
    this.processInstance.id,
    pages,
    this.getActivityParams(activityIds)
  ).then((function(data) {
    this.tasks = data.list;

    return data;
  }).bind(this));
};

ProcessInstanceRuntimeTab.prototype.getActivityParams = function(activityIds) {
  if (!activityIds || !activityIds.length) {
    return {};
  }

  return {
    activityIdIn: activityIds
  };
};
