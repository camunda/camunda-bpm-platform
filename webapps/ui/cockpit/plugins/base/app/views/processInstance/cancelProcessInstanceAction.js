'use strict';

var fs = require('fs');

var dialogTemplate = fs.readFileSync(__dirname + '/cancel-process-instance-dialog.html', 'utf8');
var actionTemplate = fs.readFileSync(__dirname + '/cancel-process-instance-action.html', 'utf8');

module.exports = function(ngModule) {
  ngModule.controller('CancelProcessInstanceActionController', [
    '$scope', 'search', 'Uri', '$uibModal',
    function($scope,   search,   Uri,   $modal) {

      $scope.openDialog = function() {
        $modal.open({
          resolve: {
            processData: function() { return $scope.processData; },
            processInstance: function() { return $scope.processInstance; }
          },
          controller: 'CancelProcessInstanceController',
          template: dialogTemplate
        });
      };
    }]);

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'cancel-process-instance-action',
      label: 'PLUGIN_CANCEL_PROCESS_DELETE_ACTION',
      template: actionTemplate,
      controller: 'CancelProcessInstanceActionController',
      priority: 20
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  ngModule.config(Configuration);
};
