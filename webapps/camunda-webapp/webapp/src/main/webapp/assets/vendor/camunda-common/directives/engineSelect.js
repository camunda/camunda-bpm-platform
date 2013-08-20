ngDefine('camunda.common.directives', [
  'angular', 
  'require'
], function(module, angular, require) {

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
  }];

  var EngineSelectDirective = function() {
    return {
      templateUrl: require.toUrl('./engineSelect.html'),
      replace: true,
      controller: ProcessEngineSelectionController, 
      link: function(scope, element, attrs) {

        var divider;

        scope.$watch(attrs['ngShow'], function(newValue) {
          if (newValue && !divider) {
            divider = $('<li class="divider-vertical"></li>').insertAfter(element);
          }

          if (!newValue && divider) {
            divider.remove();
            divider = null;
          }
        });

        scope.$on('$destroy', function() {
          if (divider) {
            divider.remove();
          }
        });
      }
    };
  };

  module
    .directive('engineSelect', EngineSelectDirective);
});