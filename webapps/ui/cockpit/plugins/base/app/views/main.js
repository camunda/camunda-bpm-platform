/**
 * @namespace cam.cockpit.plugin.base.views
 */
'use strict';

var angular = require('angular'),

    // dashboard
    processDefinitions = require('./dashboard/process-definitions'),

    // process definition
    processInstanceTable = require('./processDefinition/processInstanceTable'),
    calledProcessDefinitionTable = require('./processDefinition/calledProcessDefinitionTable'),
    updateSuspensionStateAction = require('./processDefinition/updateSuspensionStateAction'),
    updateSuspensionStateDialog = require('./processDefinition/updateSuspensionStateDialog'),
    activityInstanceStatisticsOverlay = require('./processDefinition/activityInstanceStatisticsOverlay'),

    // process instance
    variableInstancesTab = require('./processInstance/variableInstancesTab'),
    variableInstanceUploadDialog = require('./processInstance/variableInstanceUploadDialog'),
    variableInstanceInspectDialog = require('./processInstance/variableInstanceInspectDialog'),
    incidentsTab = require('./processInstance/incidentsTab'),
    calledProcessInstanceTable = require('./processInstance/calledProcessInstanceTable'),
    userTasksTable = require('./processInstance/userTasksTable'),
    jobRetryBulkAction = require('./processInstance/jobRetryBulkAction'),
    jobRetryBulkDialog = require('./processInstance/jobRetryBulkDialog'),
    jobRetryDialog = require('./processInstance/jobRetryDialog'),
    cancelProcessInstanceAction = require('./processInstance/cancelProcessInstanceAction'),
    cancelProcessInstanceDialog = require('./processInstance/cancelProcessInstanceDialog'),
    addVariableAction = require('./processInstance/addVariableAction'),
    addVariableDialog = require('./processInstance/addVariableDialog'),
    updateSuspensionStateActionPI = require('./processInstance/updateSuspensionStateAction'),
    updateSuspensionStateDialogPI = require('./processInstance/updateSuspensionStateDialog'),
    activityInstanceStatisticsOverlayPI = require('./processInstance/activityInstanceStatisticsOverlay');

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

  module.exports = ngModule;
