/* global ngDefine: false */
ngDefine('cockpit.directives', [], function(module) {
  'use strict';

  module.directive('viewPills', [
  function() {
    var ViewPillsController = [
      '$scope',
      'Views',
      '$location',
    function($scope, Views, $location) {
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
'  <li ng-repeat="provider in providers" ng-class="{ active: isActive(provider) }" class="{{ provider.id }}">' +
'    <a ng-href="{{ getUrl(provider) }}">{{ provider.label }}</a>' +
'  </li>' +
'</ul>',
      replace: true,
      controller: ViewPillsController
    };
  }]);
});
