define([
  'angular'
], function(
  angular
) {
  'use strict';

  var TasklistApp = (function() {

    function TasklistApp() {

      this.refreshProvider = null;

    }

    return TasklistApp;

  })();

  return [
    '$scope',
  function(
    $scope
  ) {

    // create a new tasklistApp
    $scope.tasklistApp = new TasklistApp();

  }];

});
