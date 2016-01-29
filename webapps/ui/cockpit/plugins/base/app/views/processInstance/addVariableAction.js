'use strict';

var fs = require('fs');

var actionTemplate = fs.readFileSync(__dirname + '/add-variable-action.html', 'utf8');
var dialogTemplate = fs.readFileSync(__dirname + '/add-variable-dialog.html', 'utf8');
var angular = require('angular');

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'add-variable-action',
      label: 'Add Variable Action',
      template: actionTemplate,
      controller: [
              '$scope', '$modal',
      function($scope,   $modal) {
        $scope.openDialog = function () {
          var dialog = $modal.open({
            scope: $scope,
            resolve: {
              processData: function() { return $scope.processData; },
              processInstance: function() { return $scope.processInstance; }
            },
            controller: 'AddVariableController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {

            // dialog closed. YEA!
            if (result === 'SUCCESS') {
              // refresh filter and all views
              $scope.processData.set('filter', angular.extend({}, $scope.filter));
            }
          });
        };
      }],
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.exports = Configuration;
