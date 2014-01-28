ngDefine('cockpit.plugin.base.views', function(module) {

   function CalledProcessInstanceController ($scope, PluginProcessInstanceResource) {

    // input: processInstance, processData

    var calledProcessInstanceData = $scope.processData.newChild($scope);
    var processInstance = $scope.processInstance;

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

      PluginProcessInstanceResource.getCalledProcessInstances({id: $scope.processInstance.id}, filter).$then(function(response) {

        angular.forEach(response.data, function (calledInstance) {
          var instance = instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
          calledInstance.instance = instance;
        });

        $scope.calledProcessInstances = response.data;
      });
    }

    $scope.selectActivity = function(activityId, event) {
      event.preventDefault();
      $scope.processData.set('filter', angular.extend({}, $scope.filter, {
          activityInstanceIds: [activityId],
          activityIds: [activityId.split(':').shift()]
        }));
    };
  };

  module.controller('CalledProcessInstanceController', [ '$scope', 'PluginProcessInstanceResource', CalledProcessInstanceController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.live.tab', {
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
