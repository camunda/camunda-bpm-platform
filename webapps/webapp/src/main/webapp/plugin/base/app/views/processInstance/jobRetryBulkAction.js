ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

  function JobRetryActionController ($scope, $http, search, Uri, $dialog) {

    $scope.openDialog = function () {
      var dialog = $dialog.dialog({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'JobRetriesController',
        templateUrl: require.toUrl('./job-retry-bulk-dialog.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
      });
    };

  };

  module.controller('JobRetryActionController', [ '$scope', '$http', 'search', 'Uri', '$dialog', JobRetryActionController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'job-retry-action',
      label: 'Job Retry Action',
      url: 'plugin://base/static/app/views/processInstance/job-retry-bulk-action.html',
      controller: 'JobRetryActionController',
      priority: 15
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
