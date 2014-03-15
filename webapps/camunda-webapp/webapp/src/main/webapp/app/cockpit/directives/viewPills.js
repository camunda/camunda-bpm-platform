ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  'use strict';

  module.directive('viewPills', [ '$location', 'Views', function($location, Views) {
    
    var ViewPillsController = [ '$scope', 'Views', '$location', function($scope, Views, $location) {
      var providers = Views.getProviders({ component: $scope.id });
      $scope.providers = providers;

      $scope.isActive = function(provider) {
        return $location.path().indexOf('/' + provider.id) != -1;
      };

      $scope.getUrl = function(provider) {
        return '#' + $location.path().replace(/[^\/]*$/, provider.id);
      };
    }];

    return {
      restrict: 'EAC',
      scope: {
        id: '@'
      },
      template: 
'<ul class="nav nav-pills">' +
'  <li ng-repeat="provider in providers" ng-class="{ active: isActive(provider) }"><a ng-href="{{ getUrl(provider) }}">{{ provider.label }}</a></li>' +
'</ul>',
      replace: true,
      controller: ViewPillsController
    };
  }]);
});