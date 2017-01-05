'use strict';

module.exports = [
  '$scope',
  '$http',
  'Uri',
  'details',
  function(
    $scope,
    $http,
    Uri,
    details
  ) {

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.value = null;
    $scope.valueDeserialized = null;
    $scope.deserializationError = null;
    $scope.type = null;
    $scope.dataFormat = null;
    $scope.variable = details;
    $scope.selectedTab = 'serialized';

    switch ($scope.variable.type) {
    case 'Object':
      $scope.type = $scope.variable.valueInfo.objectTypeName;
      $scope.value = $scope.variable.value;
      $scope.dataFormat = $scope.variable.valueInfo.serializationDataFormat;

        // attempt fetching the deserialized value
      $http({
        method: 'GET',
        url: Uri.appUri('engine://engine/:engine'+$scope.variable._links.self.href)
      }).success(function(data) {
        $scope.valueDeserialized = JSON.stringify(data.value);
      }).error(function(data) {
        $scope.deserializationError = data.message;
      });

      break;

    default:
      $scope.value = $scope.variable.value;
    }

    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
    };

  }];
