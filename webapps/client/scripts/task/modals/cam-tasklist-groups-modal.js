define([
], function(
) {
  'use strict';

  return [
    '$scope',
    'taskMetaData',
  function(
    $scope,
    taskMetaData
  ) {
    // setup //////////////////////////////////////////////

    var groupsChanged = false;

    $scope.taskGroupsData = taskMetaData.newChild($scope);

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.close = function () {
      if (groupsChanged) {
        return $scope.$close();
      }

      $scope.$dismiss();
    };

  }];

});
