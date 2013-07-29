ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $location, $q, PluginProcessDefinitionResource) {

    // input processDefinition, selection

    var parentProcessDefinitionId = $location.search().parentProcessDefinitionId || null;
    var activityIds = null; 
    
    $scope.$watch(function () { return $location.search().bpmnElements; }, function (newValue) {
      activityIds = [];

      if (newValue && angular.isString(newValue)) {
        activityIds = newValue.split(',');
      } else if (newValue && angular.isArray(newValue)) {
        activityIds = newValue;
      }

      updateView();
    });

    function updateView() {
      function waitForBpmnElements () {
        var deferred = $q.defer();

        $scope.$watch('processDefinition.bpmnElements', function (newValue) {
          if (newValue) {
            deferred.resolve(newValue);
          }
        });

        return deferred.promise;
      }

      function extractBpmnElements (processDefinitions) {
        angular.forEach(processDefinitions, function (def) {
          var callActivities = def.calledFromActivityIds;
          def.calledFromActivityIds = [];

          angular.forEach(callActivities, function (callActivityId) {
            var bpmnElement = $scope.processDefinition.bpmnElements[callActivityId];
            var tmp = {activityId: callActivityId, bpmnElement: bpmnElement};
            def.calledFromActivityIds.push(tmp);
          });

        });
      }

      PluginProcessDefinitionResource.getCalledProcessDefinitions({id: $scope.processDefinition.id},
        {
          activityIdIn: activityIds,
          superProcessDefinitionId: parentProcessDefinitionId
        }).$then(function (response) {
        if ($scope.processDefinition.bpmnElements) {
          extractBpmnElements(response.data);
        } else {
          waitForBpmnElements().then(function () {
            extractBpmnElements(response.data);
          });
        }

        $scope.calledProcessDefinitions = response.data;
      });      
    }
    
    $scope.selectBpmnElement = function (bpmnElement) {
      $scope.selection.view = {bpmnElements: [ bpmnElement ], scrollToBpmnElement: bpmnElement};
    };

  };

  Controller.$inject = [ '$scope', '$location', '$q', 'PluginProcessDefinitionResource' ];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'call-process-definitions-table',
      label: 'Called Process Definitions',
      url: 'plugin://base/static/app/views/processDefinition/called-process-definition-table.html',
      controller: Controller,
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
