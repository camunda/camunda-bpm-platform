ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

  var Controller = [ '$scope', '$dialog',
    function($scope, $dialog) {

    $scope.openDialog = function () {
      var dialog = $dialog.dialog({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'UpdateProcessInstanceSuspensionStateController',
        templateUrl: require.toUrl('./update-suspension-state-dialog.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
        if (result.status === 'SUCCESS') {
          $scope.processInstance.suspended = result.suspended;

          $scope.processData.set('filter', angular.extend({}, $scope.filter));
        }

      });
    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      url: 'plugin://base/static/app/views/processInstance/update-suspension-state-action.html',
      controller: Controller,
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);

});
