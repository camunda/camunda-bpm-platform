'use strict';

module.exports = [
  '$scope', 'exposeScopeProperties', 'externalTasks',
  ProcessInstanceRuntimeTab
];

function ProcessInstanceRuntimeTab($scope, exposeScopeProperties, externalTasks) {
  exposeScopeProperties($scope, this, ['processInstance', 'processData']);

  this.externalTasks = externalTasks;
}

ProcessInstanceRuntimeTab.prototype.onLoad = function(pages, params) {
  return this.externalTasks.getActiveExternalTasksForProcess(
    this.processInstance.id,
    pages,
    params
  ).then((function(data) {
    this.tasks = data.list;

    return data;
  }).bind(this));
};
