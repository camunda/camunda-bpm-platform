/* global ngDefine: false */
ngDefine('cockpit.directives', [
  'cockpit/util/routeUtil'
  ], function(module) {
  'use strict';

  var routeUtil = require('cockpit/util/routeUtil');

  module.directive('viewPills', [
  function() {
    var ViewPillsController = [
      '$scope',
      'Views',
      '$location',
    function($scope, Views, $location) {
      var providers = Views.getProviders({ component: $scope.id });
      $scope.providers = providers;

      var isActive = $scope.isActive = function(provider) {
        return $location.path().indexOf('/' + provider.id) != -1;
      };

      $scope.getUrl = function(provider) {
        var replacement = provider.id,
            currentPath = $location.path(),
            currentSearch = $location.search(),
            keepSearchParams = !isActive(provider) ? provider.keepSearchParams : true;

        return '#' + routeUtil.replaceLastPathFragment(replacement, currentPath, currentSearch, keepSearchParams);

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
