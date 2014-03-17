ngDefine('cockpit.plugin.jobDefinition.views', ['require'], function(module, require) {

  var Controller = [
    '$scope',
    '$rootScope',
    'search',
    '$dialog',
  function($scope, $rootScope, search, $dialog) {

    $scope.openSuspensionStateDialog = function (jobDefinition) {
      var dialog = $dialog.dialog({
        resolve: {
          jobDefinition: function() { return jobDefinition; }
        },
        controller: 'JobDefinitionSuspensionStateController',
        templateUrl: require.toUrl('./job-definition-suspension-state-dialog.html')
      });

      dialog.open().then(function(result) {
        // dialog closed. YEA!
        if (result.status === 'SUCCESS') {
          if (result.executeImmediately) {
            jobDefinition.suspended = result.suspended;
            $rootScope.$broadcast('$jobDefinition.suspensionState.changed', $scope.jobDefinition);
          }

          $scope.processData.set('filter', angular.extend({}, $scope.filter));
        }
      });

    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
      id: 'update-suspension-state',
      url: 'plugin://jobDefinition/static/app/views/processDefinition/suspension-state-action.html',
      controller: Controller,
      priority: 50
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
