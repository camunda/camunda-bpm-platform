ngDefine('cockpit.directives', [
  'angular'
], function(module, angular) {

  var ProcessEngineSelectionController = [
    '$scope', '$http', '$location', '$window', 'Uri', 'Notifications',
    function($scope, $http, $location, $window, Uri, Notifications) {

    var current = Uri.appUri(':engine');
    var enginesByName = {};

    $http.get(Uri.appUri('engine://engine/')).then(function(response) {
      $scope.engines = response.data;

      angular.forEach($scope.engines , function(engine) {
        enginesByName[engine.name] = engine;
      });

      $scope.currentEngine = enginesByName[current];

      if (!$scope.currentEngine) {
        Notifications.addError({ status: 'Not found', message: 'The process engine you are trying to access does not exist' });
        $location.path('/dashboard')
      }
    });

    $scope.$watch('currentEngine', function(engine) {
      if (engine && current !== engine.name) {
        $window.location.href = Uri.appUri("app://../" + engine.name + "/");
      }
    });
  }];

  var EngineSelectDirective = function() {
    return {
      templateUrl: 'directives/engineSelect.html',
      replace: true,
      controller: ProcessEngineSelectionController
    };
  };

  module
    .directive('engineSelect', EngineSelectDirective);
});