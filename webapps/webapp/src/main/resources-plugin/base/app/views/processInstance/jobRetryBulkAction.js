/* global define: false */
define(['text!./job-retry-bulk-dialog.html', 'text!./job-retry-bulk-action.html'], function(dialogTemplate, actionTemplate) {
  'use strict';

  return function(ngModule) {
    ngModule.controller('JobRetryActionController', [
              '$scope', '$modal',
      function($scope,   $modal) {
        $scope.openDialog = function () {
          $modal.open({
            resolve: {
              processData: function() { return $scope.processData; },
              processInstance: function() { return $scope.processInstance; }
            },
            size: 'lg',
            controller: 'JobRetriesController',
            template: dialogTemplate
          });
        };
      }]);

      var Configuration = function PluginConfiguration(ViewsProvider) {
        ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
          id: 'job-retry-action',
          label: 'Job Retry Action',
          template: actionTemplate,
          controller: 'JobRetryActionController',
          priority: 15
        });
      };

      Configuration.$inject = ['ViewsProvider'];

      ngModule.config(Configuration);
  };

});
