'use strict';

var angular = require('angular');
var fs = require('fs');

var actionTemplate = fs.readFileSync(__dirname + '/add-variable-action.html', 'utf8');
var addTemplate = require('../../../../../client/scripts/components/variables/variable-add-dialog');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
    id: 'add-variable-action',
    label: 'Add Variable Action',
    template: actionTemplate,
    controller: [
      '$scope', '$modal', '$rootScope',
      function($scope, $modal, $rootScope) {
        $scope.openDialog = function() {
          var dialog = $modal.open({
            scope: $scope,
            resolve: {
              instance: function() { return $scope.processInstance; },
              isProcessInstance: function() { return true; }
            },
            controller: addTemplate.controller,
            template: addTemplate.template
          });

          dialog.result.then(function(result) {

            // dialog closed. YEA!
            if (result === 'SUCCESS') {
              // refresh filter and all views
              $scope.processData.set('filter', angular.extend({}, $scope.filter));
              $rootScope.$broadcast('addVariableNotification');
            }
          });
        };
      }],
    priority: 10
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
