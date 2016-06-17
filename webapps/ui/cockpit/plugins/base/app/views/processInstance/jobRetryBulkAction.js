'use strict';

var fs = require('fs');

var dialogTemplate = fs.readFileSync(__dirname + '/job-retry-bulk-dialog.html', 'utf8');
var actionTemplate = fs.readFileSync(__dirname + '/job-retry-bulk-action.html', 'utf8');

module.exports = function(ngModule) {
  ngModule.controller('JobRetryActionController', [
    '$scope', '$modal',
    function($scope,   $modal) {
      $scope.openDialog = function() {
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
