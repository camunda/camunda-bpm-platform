ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

  function AddVariableActionController ($scope, $http, search, Uri, $dialog) {

    $scope.openDialog = function () {
      var dialog = $dialog.dialog({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'AddVariableController',
        templateUrl: require.toUrl('./add-variable-dialog.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
        if (result === "SUCCESS") {
          // refresh filter and all views
          $scope.processData.set('filter', angular.extend({}, $scope.filter));
        }
      });
    };

  };

  module.controller('AddVariableActionController', [ '$scope', '$http', 'search', 'Uri', '$dialog', AddVariableActionController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.action', {
      id: 'add-variable-action',
      label: 'Add Variable Action',
      url: 'plugin://base/static/app/views/processInstance/add-variable-action.html',
      controller: 'AddVariableActionController',
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
