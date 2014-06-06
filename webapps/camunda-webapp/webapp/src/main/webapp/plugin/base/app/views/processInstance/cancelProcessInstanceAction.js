/* global ngDefine: false */
ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {
  'use strict';

  module.controller('CancelProcessInstanceActionController', [
          '$scope', '$http', 'search', 'Uri', '$modal',
  function($scope,   $http,   search,   Uri,   $modal) {

    $scope.openDialog = function () {
      $modal.open({
        resolve: {
          processData: function() { return $scope.processData; },
          processInstance: function() { return $scope.processInstance; }
        },
        controller: 'CancelProcessInstanceController',
        templateUrl: require.toUrl('./cancel-process-instance-dialog.html')
      });
    };
  }]);

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
