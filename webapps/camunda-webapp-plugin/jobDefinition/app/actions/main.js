define(['angular',

  // override job priority action
  './override-job-priority/override-job-priority-action',
  './override-job-priority/override-job-priority-dialog',

  // bulk override job priority action
  './bulk-override-job-priority/bulk-override-job-priority-action',
  './bulk-override-job-priority/bulk-override-job-priority-dialog'

], function(angular,

  // override job priority action
  overrideJobPriorityAction,
  overrideJobPriorityDialog,

  // bulk override job priority action
  bulkOverrideJobPriorityAction,
  bulkOverrideJobPriorityDialog) {

  var ngModule = angular.module('cockpit.plugin.jobDefinition.actions', []);

  // override job priority action  
  ngModule.config(overrideJobPriorityAction);
  ngModule.controller('JobDefinitionOverrideJobPriorityController', overrideJobPriorityDialog);

  // bulk override job priority action
  ngModule.config(bulkOverrideJobPriorityAction);
  ngModule.controller('BulkJobDefinitionOverrideJobPriorityController', bulkOverrideJobPriorityDialog);

  return ngModule;
});
