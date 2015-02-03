/* global define: false */

/**
 * @namespace cam.cockpit.plugin.base.views
 */
define([
  'angular',

  // dashboard
  './dashboard/process-definitions',

  // process definition
  './processDefinition/processInstanceTable',
  './processDefinition/calledProcessDefinitionTable',
  './processDefinition/updateSuspensionStateAction',
  './processDefinition/updateSuspensionStateDialog',
  './processDefinition/activityInstanceStatisticsOverlay',

  // process instance
  './processInstance/variableInstancesTab',
  './processInstance/variableInstanceUploadDialog',
  './processInstance/variableInstanceInspectDialog',
  './processInstance/incidentsTab',
  './processInstance/calledProcessInstanceTable',
  './processInstance/userTasksTable',
  './processInstance/jobRetryBulkAction',
  './processInstance/jobRetryBulkDialog',
  './processInstance/jobRetryDialog',
  './processInstance/cancelProcessInstanceAction',
  './processInstance/cancelProcessInstanceDialog',
  './processInstance/addVariableAction',
  './processInstance/addVariableDialog',
  './processInstance/updateSuspensionStateAction',
  './processInstance/updateSuspensionStateDialog',
  './processInstance/activityInstanceStatisticsOverlay'
], function(
  angular,

  // dashboard
  processDefinitions,

  // process definition
  processInstanceTable,
  calledProcessDefinitionTable,
  updateSuspensionStateAction,
  updateSuspensionStateDialog,
  activityInstanceStatisticsOverlay,

  // process instance
  variableInstancesTab,
  variableInstanceUploadDialog,
  variableInstanceInspectDialog,
  incidentsTab,
  calledProcessInstanceTable,
  userTasksTable,
  jobRetryBulkAction,
  jobRetryBulkDialog,
  jobRetryDialog,
  cancelProcessInstanceAction,
  cancelProcessInstanceDialog,
  addVariableAction,
  addVariableDialog,
  updateSuspensionStateActionPI,
  updateSuspensionStateDialogPI,
  activityInstanceStatisticsOverlayPI) {
  'use strict';
  var ngModule = angular.module('cockpit.plugin.base.views', []);

  ngModule.config(processDefinitions);

  ngModule.config(processInstanceTable);
  ngModule.config(calledProcessDefinitionTable);
  ngModule.config(updateSuspensionStateAction);
  ngModule.controller('UpdateProcessDefinitionSuspensionStateController', updateSuspensionStateDialog);
  ngModule.config(activityInstanceStatisticsOverlay);

  variableInstancesTab(ngModule);
  ngModule.controller('VariableInstanceUploadController', variableInstanceUploadDialog);
  ngModule.controller('VariableInstanceInspectController', variableInstanceInspectDialog);
  ngModule.config(incidentsTab);
  calledProcessInstanceTable(ngModule);
  userTasksTable(ngModule);
  jobRetryBulkAction(ngModule);
  ngModule.controller('JobRetriesController', jobRetryBulkDialog);
  ngModule.controller('JobRetryController', jobRetryDialog);
  cancelProcessInstanceAction(ngModule);
  ngModule.controller('CancelProcessInstanceController', cancelProcessInstanceDialog);
  ngModule.config(addVariableAction);
  ngModule.controller('AddVariableController', addVariableDialog);
  ngModule.config(updateSuspensionStateActionPI);
  ngModule.controller('UpdateProcessInstanceSuspensionStateController', updateSuspensionStateDialogPI);
  ngModule.config(activityInstanceStatisticsOverlayPI);

  return ngModule;
});
