/* global ngDefine: false */

/**
 * @namespace cam.cockpit.plugin.base.views
 */
ngDefine('cockpit.plugin.base.views', [
  // dashboard
  './dashboard/processDefinitionList',
  './dashboard/processDefinitionTiles',

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
], function() {});
