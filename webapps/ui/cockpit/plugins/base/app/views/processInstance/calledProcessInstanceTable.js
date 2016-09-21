'use strict';

var fs = require('fs');
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/called-process-instance-table.html', 'utf8');

module.exports = function(ngModule) {
  ngModule.controller('CalledProcessInstanceController', [
    '$scope', 'PluginProcessInstanceResource',
    function($scope,   PluginProcessInstanceResource) {

        // input: processInstance, processData

      var calledProcessInstanceData = $scope.processData.newChild($scope);
        // var processInstance = $scope.processInstance;

      var filter = null;

      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityInstanceIdIn');

      calledProcessInstanceData.observe([ 'filter', 'instanceIdToInstanceMap' ], function(newFilter, instanceIdToInstanceMap) {
        updateView(newFilter, instanceIdToInstanceMap);
      });

      function updateView(newFilter, instanceIdToInstanceMap) {
        filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityIds;
        delete filter.scrollToBpmnElement;

          // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
        filter.activityInstanceIdIn = filter.activityInstanceIds;
        delete filter.activityInstanceIds;

        $scope.calledProcessInstances = null;

        $scope.loadingState = 'LOADING';
        PluginProcessInstanceResource
          .processInstances({
            id: $scope.processInstance.id
          }, filter)
          .$promise.then(function(response) {

            // angular.forEach(response.data, function (calledInstance) {
            angular.forEach(response, function(calledInstance) {
              var instance = instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
              calledInstance.instance = instance;
            });

            $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
            $scope.calledProcessInstances = response;
          });
      }
    }]);

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'called-process-instances-tab',
      label: 'Called Process Instances',
      template: template,
      controller: 'CalledProcessInstanceController',
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  ngModule.config(Configuration);
};
