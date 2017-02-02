/**
 * @namespace cam.cockpit.plugin.base.views
 */
'use strict';

var angular = require('angular'),
    camCommon = require('cam-common'),
    // dashboard
    dashboardDeployments = require('./dashboard/deployments'),
    dashboardReports = require('./dashboard/reports'),
    dashboardBatches = require('./dashboard/batches'),
    dashboardProcesses = require('./dashboard/processes'),
    dashboardDecisions = require('./dashboard/decisions'),
    dashboardTasks = require('./dashboard/tasks'),

    // processes dashboard
    processDefinitions = require('./processesDashboard/process-definitions'),

    // process definition
    processInstanceTable = require('./processDefinition/processInstanceTable'),
    calledProcessDefinitionTable = require('./processDefinition/calledProcessDefinitionTable'),
    updateSuspensionStateAction = require('./processDefinition/updateSuspensionStateAction'),
    updateSuspensionStateDialog = require('./processDefinition/updateSuspensionStateDialog'),
    activityInstanceStatisticsOverlay = require('./processDefinition/activityInstanceStatisticsOverlay'),

    // process instance
    variableInstancesTab = require('./processInstance/variableInstancesTab'),
    incidentsTab = require('./processInstance/incidentsTab'),
    calledProcessInstanceTable = require('./processInstance/calledProcessInstanceTable'),
    userTasksTable = require('./processInstance/userTasksTable'),
    jobRetryBulkAction = require('./processInstance/jobRetryBulkAction'),
    jobRetryBulkDialog = require('./processInstance/jobRetryBulkDialog'),
    jobRetryDialog = require('./processInstance/jobRetryDialog'),
    externalTaskRetryDialog = require('./processInstance/externalTaskRetryDialog'),
    cancelProcessInstanceAction = require('./processInstance/cancelProcessInstanceAction'),
    cancelProcessInstanceDialog = require('./processInstance/cancelProcessInstanceDialog'),
    addVariableAction = require('./processInstance/addVariableAction'),
    updateSuspensionStateActionPI = require('./processInstance/updateSuspensionStateAction'),
    updateSuspensionStateDialogPI = require('./processInstance/updateSuspensionStateDialog'),
    activityInstanceStatisticsOverlayPI = require('./processInstance/activityInstanceStatisticsOverlay'),
    incidentJobRetryAction = require('./processInstance/incidentJobRetryAction'),
    incidentExternalTaskRetryAction = require('./processInstance/incident-externalTask-retry-action');

var ngModule = angular.module('cockpit.plugin.base.views', [
  camCommon.name
]);

ngModule.config(dashboardDeployments);
ngModule.config(dashboardReports);
ngModule.config(dashboardBatches);
ngModule.config(dashboardProcesses);
ngModule.config(dashboardDecisions);
ngModule.config(dashboardTasks);

ngModule.config(processDefinitions);

ngModule.config(processInstanceTable);
ngModule.config(calledProcessDefinitionTable);
ngModule.config(updateSuspensionStateAction);
ngModule.controller('UpdateProcessDefinitionSuspensionStateController', updateSuspensionStateDialog);
ngModule.config(activityInstanceStatisticsOverlay);

variableInstancesTab(ngModule);
ngModule.config(incidentsTab);
calledProcessInstanceTable(ngModule);
userTasksTable(ngModule);
jobRetryBulkAction(ngModule);
ngModule.controller('JobRetriesController', jobRetryBulkDialog);
ngModule.controller('JobRetryController', jobRetryDialog);
ngModule.controller('ExternalTaskRetryController', externalTaskRetryDialog);
cancelProcessInstanceAction(ngModule);
ngModule.controller('CancelProcessInstanceController', cancelProcessInstanceDialog);
ngModule.config(addVariableAction);
ngModule.config(updateSuspensionStateActionPI);
ngModule.controller('UpdateProcessInstanceSuspensionStateController', updateSuspensionStateDialogPI);
ngModule.config(activityInstanceStatisticsOverlayPI);
ngModule.config(incidentJobRetryAction);
ngModule.config(incidentExternalTaskRetryAction);

module.exports = ngModule;
