define(['angular',
       './processDefinition',
       './processInstance'],
function(angular,
         processDefinition,
         processInstance) {
  var ngModule = angular.module('cockpit.plugin.base.resources', []);

  ngModule.factory('PluginProcessDefinitionResource', processDefinition);
  ngModule.factory('PluginProcessInstanceResource', processInstance);

  return ngModule;
});
