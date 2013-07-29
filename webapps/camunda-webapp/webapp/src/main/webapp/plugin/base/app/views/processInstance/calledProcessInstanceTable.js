ngDefine('cockpit.plugin.base.views', function(module) {

   function CalledProcessInstanceController ($scope, $location, $q, PluginProcessInstanceResource) {

    // input: processInstanceId, selection, processInstance
    
    var activityInstanceIds = null;

    $scope.$watch(function () { return $location.search().activityInstances; }, function (newValue) {
      activityInstanceIds = [];

      if (newValue && angular.isString(newValue)) {
        activityInstanceIds = newValue.split(',');
      } else if (newValue && angular.isArray(newValue)) {
        activityInstanceIds = newValue;
      }

      updateView();
    });

    function updateView() {

      function waitForInstanceIdToInstanceMap() {
        var deferred = $q.defer();

        $scope.$watch('processInstance.instanceIdToInstanceMap', function (newValue) {
          if (newValue) {
            deferred.resolve(newValue);
          }
        });

        return deferred.promise;
      }

      function setInstances(calledProcessInstances, instances) {
        angular.forEach(calledProcessInstances, function (calledInstance) {
          var instance = instances[calledInstance.callActivityInstanceId];
          calledInstance.instance = instance;
        });

      }

      PluginProcessInstanceResource.getCalledProcessInstances({id: $scope.processInstanceId}, {
        activityInstanceIdIn: activityInstanceIds
      }).$then(function(response) {

        if ($scope.processInstance.instanceIdToInstanceMap) {
          setInstances(response.data, $scope.processInstance.instanceIdToInstanceMap);
        } else {
          waitForInstanceIdToInstanceMap().then(function (result) {
            setInstances(response.data, result);
          });
        }

        $scope.calledProcessInstances = response.data;
      });      
    }

    $scope.selectActivityInstance = function (activityInstance) {
      $scope.selection.view = {activityInstances: [ activityInstance ], scrollTo: {activityId: activityInstance.activityId || activityInstances.targetActivityId}};
    };

  };

  module.controller('CalledProcessInstanceController', [ '$scope', '$location', '$q', 'PluginProcessInstanceResource', CalledProcessInstanceController ]);

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
