'use strict';

module.exports = [
  '$scope', 'exposeScopeProperties', 'externalTasks', 'observeBpmnElements','$translate', 'localConf',
  ProcessInstanceRuntimeTab
];

function ProcessInstanceRuntimeTab($scope, exposeScopeProperties, externalTasks, observeBpmnElements, $translate, localConf) {
  exposeScopeProperties($scope, this, ['processInstance', 'processData']);
  observeBpmnElements($scope, this);

  this.translate = $translate;
  this.localConf = localConf;
  this.externalTasks = externalTasks;
  this.query = null;
  this.pages = null;
  this.activityIds = null;

  this.sorting = this._loadLocal({ sortBy: 'taskPriority', sortOrder: 'asc' });

  this.headColumns = [
    { class: 'state',     request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_EXTERNAL_TASK_ID')},
    { class: 'activity',  request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_ACTIVITY')},
    { class: 'retries',   request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_RETRIES')},
    { class: 'worker-id', request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_WORKER_ID')},
    { class: 'expiration',request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_LOCK_EXPIRATION_TIME')},
    { class: 'topic',     request: '', sortable: false, content: this.translate.instant('PLUGIN_EXT_TOPIC')},
    { class: 'priority',  request: 'taskPriority', sortable: true, content: this.translate.instant('PLUGIN_EXT_PRIORITY')}
  ];

}

ProcessInstanceRuntimeTab.prototype._loadLocal = function(defaultValue) {
  return this.localConf.get('sortExternalTaskRuntimeTab', defaultValue);
};

ProcessInstanceRuntimeTab.prototype._saveLocal = function(sorting) {
  return this.localConf.set('sortExternalTaskRuntimeTab', sorting);
};

ProcessInstanceRuntimeTab.prototype.onSortChange = function(sorting) {
  this.sorting = sorting;
  this._saveLocal(this.sorting);
  this.onLoad();
};

ProcessInstanceRuntimeTab.prototype.onLoad = function(pages, activityIds) {
  this.pages = pages || this.pages;
  this.activityIds = activityIds || this.activityIds;

  return this.externalTasks.getActiveExternalTasksForProcess(
    this.processInstance.id,
    this.pages,
    this.sorting,
    this.getActivityParams(this.activityIds)
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
