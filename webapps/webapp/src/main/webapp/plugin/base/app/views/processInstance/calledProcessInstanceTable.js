ngDefine('cockpit.plugin.base.views', function(module) {

   function CalledProcessInstanceController ($scope, $location, PluginProcessInstanceResource) {

    // input: selection, processInstance, processData

    var processData = $scope.processData;
    
    var activityInstanceIds = null;

    processData.get([ 'filter' ], function (filter) {
      if (!filter) {
        return;
      }

      activityInstanceIds = filter.activityInstances || [];
      updateView();
    });

    function updateView() {

      PluginProcessInstanceResource.getCalledProcessInstances({id: $scope.processInstance.id}, {
        activityInstanceIdIn: activityInstanceIds
      }).$then(function(response) {
        processData.get('instanceIdToInstanceMap', function (instanceIdToInstanceMap) {
          angular.forEach(response.data, function (calledInstance) {
            var instance = instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
            calledInstance.instance = instance;
          });
        });
        $scope.calledProcessInstances = response.data;
      });      
    }

    $scope.selectActivityInstance = function (activityInstance) {
      $scope.selection.view = {activityInstances: [ activityInstance ], scrollTo: {activityId: activityInstance.activityId || activityInstances.targetActivityId}};
    };

  };

  module.controller('CalledProcessInstanceController', [ '$scope', '$location', 'PluginProcessInstanceResource', CalledProcessInstanceController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'called-process-instances-tab',
      label: 'Called Process Instances',
      url: 'plugin://base/static/app/views/processInstance/called-process-instance-table.html',
      controller: 'CalledProcessInstanceController',
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
