  'use strict';

  module.exports = [
    '$scope',
    'taskMetaData',
    'groupsChanged',
    function(
    $scope,
    taskMetaData,
    groupsChanged
  ) {
    // setup //////////////////////////////////////////////

      $scope.taskGroupsData = taskMetaData.newChild($scope);

      $scope.groupsChanged = groupsChanged || function() {};

      $scope.$on('$locationChangeSuccess', function() {
        $scope.$dismiss();
      });

    }];
