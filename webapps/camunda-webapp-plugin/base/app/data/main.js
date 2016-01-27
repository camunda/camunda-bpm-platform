define(['angular',
        './dashboard/processDefinitionStatisticsData',
        './processDefinition/activityInstanceStatisticsData'],
function(angular, processDefinition, activityInstance) {

  var ngModule = angular.module('cockpit.plugin.base.data', []);

  ngModule.config(processDefinition);
  ngModule.config(activityInstance);

  return ngModule;
});
