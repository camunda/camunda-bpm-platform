/* global ngDefine: false */
ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {
  'use strict';
  module.controller('JobRetryActionController', [
          '$scope', '$modal',
  function($scope,   $modal) {
    $scope.openDialog = function () {
      $modal.open({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'JobRetriesController',
        templateUrl: require.toUrl('./job-retry-bulk-dialog.html')
      });
    };
  }]);

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
