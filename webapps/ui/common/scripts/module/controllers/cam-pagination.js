'use strict';

module.exports = ['$scope', 'search', 'paginationUtils', 'exposeScopeProperties', CamPagination];

function CamPagination($scope, search, paginationUtils, exposeScopeProperties) {
  paginationUtils.initializePaginationInController($scope, search, function() {
    $scope.onPaginationChange({pages: $scope.pages});
  });

  exposeScopeProperties($scope, this, ['total', 'pages']);

  $scope.onPaginationChange({pages: $scope.pages});
}
