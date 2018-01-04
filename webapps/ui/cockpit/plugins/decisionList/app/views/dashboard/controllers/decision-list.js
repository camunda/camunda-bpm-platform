'use strict';

module.exports = [
  '$scope', 'decisionList', 'Views', //'localConf', '$translate',
  function($scope, decisionList, Views /*, localConf, $translate*/) {
    $scope.loadingState = 'LOADING';
    $scope.drdDashboard = Views.getProvider({ component: 'cockpit.plugin.drd.dashboard' });
    $scope.isDrdDashboardAvailable = !!$scope.drdDashboard;

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

    $scope.drdDashboardVars = { read: [ 'drdsCount', 'drds'] };
  }
];
