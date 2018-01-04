'use strict';

module.exports = [
  '$scope', 'decisionList', 'Views', 'localConf', '$translate',
  function($scope, decisionList, Views, localConf, $translate) {
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


    // DRD table
    $scope.headColumns = [
      { class: 'name',     request: 'key'          , sortable: true, content: $translate.instant('PLGN_DRD_NAME')},
      { class: 'tenant-id', request: 'tenantId'     , sortable: true, content: $translate.instant('PLGN_DRD_TENANT_ID_COL')}
    ];

    // Default sorting
    var defaultValue = { sortBy: 'key', sortOrder: 'asc', sortReverse: false};
    $scope.sortObj   = loadLocal(defaultValue);

    // Update Table
    $scope.onSortChange = function(sortObj) {
      sortObj = sortObj || $scope.sortObj;
      // transforms sortOrder to boolean required by anqular-sorting;
      sortObj.sortReverse = sortObj.sortOrder !== 'asc';
      saveLocal(sortObj);
      $scope.sortObj = sortObj;
    };

    function saveLocal(sortObj) {
      localConf.set('sortDRDTab', sortObj);

    }
    function loadLocal(defaultValue) {
      return localConf.get('sortDRDTab', defaultValue);
    }





    $scope.drdDashboardVars = { read: [ 'drdsCount', 'drds','headColumns', 'sortObj','onSortChange' ] };
  }
];
