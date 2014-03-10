ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

  function CancelProcessInstanceActionController ($scope, $http, search, Uri, $dialog) {

    $scope.openDialog = function () {
      var dialog = $dialog.dialog({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'CancelProcessInstanceController',
        templateUrl: require.toUrl('./cancel-process-instance-dialog.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
      });
    };

  };

  module.controller('CancelProcessInstanceActionController', [ '$scope', '$http', 'search', 'Uri', '$dialog', CancelProcessInstanceActionController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'cancel-process-instance-action',
      label: 'Cancel Process Instance Action',
      url: 'plugin://base/static/app/views/processInstance/cancel-process-instance-action.html',
      controller: 'CancelProcessInstanceActionController',
      priority: 20
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
