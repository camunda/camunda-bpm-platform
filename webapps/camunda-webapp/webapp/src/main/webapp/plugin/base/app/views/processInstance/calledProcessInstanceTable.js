/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';

  module.controller('CalledProcessInstanceController', [
    '$scope',
    'PluginProcessInstanceResource',
  function($scope, PluginProcessInstanceResource) {

    // input: processInstance, processData

    var calledProcessInstanceData = $scope.processData.newChild($scope);
    // var processInstance = $scope.processInstance;

    var filter = null;

    calledProcessInstanceData.observe([ 'filter', 'instanceIdToInstanceMap' ], function (newFilter, instanceIdToInstanceMap) {
      updateView(newFilter, instanceIdToInstanceMap);
    });

    function updateView (newFilter, instanceIdToInstanceMap) {
      filter = angular.copy(newFilter);

      delete filter.page;
      delete filter.activityIds;
      delete filter.scrollToBpmnElement;

      // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
      filter.activityInstanceIdIn = filter.activityInstanceIds;
      delete filter.activityInstanceIds;

      $scope.calledProcessInstances = null;

      // deprecate `getCalledProcessInstances`
      // PluginProcessInstanceResource.getCalledProcessInstances({id: $scope.processInstance.id}, filter).$then(function(response) {
      PluginProcessInstanceResource
      .processInstances({
        id: $scope.processInstance.id
      }, filter)
      .$then(function(response) {

        angular.forEach(response.data, function (calledInstance) {
          var instance = instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
          calledInstance.instance = instance;
        });

        $scope.calledProcessInstances = response.data;
      });
    }
  }]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'called-process-instances-tab',
      label: 'Called Process Instances',
      url: 'plugin://base/static/app/views/processInstance/called-process-instance-table.html',
      controller: 'CalledProcessInstanceController',
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
