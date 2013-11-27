ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

  var Controller = [ '$scope', '$dialog',
    function($scope, $dialog) {

    $scope.openDialog = function () {
      var dialog = $dialog.dialog({
        resolve: {
          processData: function() { return $scope.processData; },
          processDefinition: function() { return $scope.processDefinition; }
        },
        controller: 'UpdateProcessDefinitionSuspensionStateController',
        templateUrl: require.toUrl('./update-suspension-state-dialog.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
        if (result.status === 'SUCCESS') {
          if (result.executeImmediately) {
            $scope.processDefinition.suspended = result.suspended;
          }

          $scope.processData.set('filter', angular.extend({}, $scope.filter));
        }

      });
    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      url: 'plugin://base/static/app/views/processDefinition/update-suspension-state-action.html',
      controller: Controller,
      priority: 50
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);

});
