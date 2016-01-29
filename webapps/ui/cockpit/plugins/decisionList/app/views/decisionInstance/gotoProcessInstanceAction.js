'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/goto-process-instance-action.html', 'utf8');

  module.exports = function(ngModule) {
    ngModule.controller('GotoProcessInstanceActionController', [
            '$scope', 'Views',
    function($scope,   Views) {

      var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });

      var hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
        return plugin.id === 'history';
      }).length > 0;

      if(hasHistoryPlugin) {
        // if we have no history plugin, then just go to the runtime view
        $scope.processInstanceLink =
            '#/process-instance/' + $scope.decisionInstance.processInstanceId + '/history' +
            '?activityInstanceIds=' + $scope.decisionInstance.activityInstanceId +
            '&activityIds=' + $scope.decisionInstance.activityId;
      } else {
        // if we have the history plugin, go to the history view and select the activity, that executed the decision
        $scope.processInstanceLink = '#/process-instance/' + $scope.decisionInstance.processInstanceId;
      }

    }]);

    var Configuration = function PluginConfiguration(ViewsProvider) {
      ViewsProvider.registerDefaultView('cockpit.decisionInstance.action', {
        id: 'goto-process-instance-action',
        label: 'Goto Process Instance Action',
        template: template,
        controller: 'GotoProcessInstanceActionController',
        priority: 20
      });
    };

    Configuration.$inject = ['ViewsProvider'];

    ngModule.config(Configuration);
  };
