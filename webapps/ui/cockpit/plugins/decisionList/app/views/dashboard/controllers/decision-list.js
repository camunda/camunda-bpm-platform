'use strict';

module.exports = [
  '$scope', 'decisionList', 'isModuleAvailable',
  function($scope, decisionList, isModuleAvailable) {
    $scope.loadingState = 'LOADING';
    $scope.isDrdAvailable = isModuleAvailable('cockpit.plugin.drd');

    decisionList
      .getDecisionsLists()
      .then(function(data) {
        $scope.loadingState = 'LOADED';

        $scope.decisionCount = data.decisions.length;
        $scope.decisions = data.decisions;

        $scope.drdsCount = data.drds.length;
        $scope.drds = data.drds;
      })
      .catch(function(err) {
        $scope.loadingError = err.message;
        $scope.loadingState = 'ERROR';

        throw err;
      });
  }
];
