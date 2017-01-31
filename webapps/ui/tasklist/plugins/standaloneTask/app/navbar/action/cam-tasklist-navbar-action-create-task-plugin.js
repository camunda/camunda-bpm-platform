'use strict';

var fs = require('fs');

var createTaskActionTemplate = fs.readFileSync(__dirname + '/cam-tasklist-navbar-action-create-task-plugin.html', 'utf8');
var createTaskModalTemplate = fs.readFileSync(__dirname + '/modals/cam-tasklist-create-task-modal.html', 'utf8');

var Controller = [
  '$scope',
  '$modal',
  '$timeout',
  function(
    $scope,
    $modal,
    $timeout
  ) {

    $scope.open = function() {
      var modalInstance = $modal.open({
        size: 'lg',
        controller: 'camCreateTaskModalCtrl',
        template: createTaskModalTemplate
      });

      modalInstance.result.then(function() {
        $scope.$root.$broadcast('refresh');
        document.querySelector('.create-task-action a').focus();
      }, function() {
        document.querySelector('.create-task-action a').focus();
      });

      // once we upgrade to a newer version of angular and angular-ui-bootstrap,
      // we can use the {{rendered}} promise to get rid of the $timeouts
      modalInstance.opened.then(function() {
        $timeout(function() {
          $timeout(function() {
            document.querySelectorAll('div.modal-content input')[0].focus();
          });
        });
      });
    };

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'create-task-action',
    template: createTaskActionTemplate,
    controller: Controller,
    priority: 200
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
