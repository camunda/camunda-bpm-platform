/* global ngDefine: false, require: false */
/* jshint unused: false */
ngDefine('cockpit.directives', ['angular'], function(module, angular) {
  'use strict';

  module.directive('camBreadcrumbsPanel', [function () {
    return {
      scope: {
        divider: '@'
      },

      restrict: 'A',

      templateUrl: require.toUrl('./app/cockpit/directives/breadcrumbs.html'),

      link: function(scope) {
        // event triggered by the breadcrumbs service when the breadcrumbs are alterated
        scope.$on('page.breadcrumbs.changed', function(ev, breadcrumbs) {
          scope.breadcrumbs = breadcrumbs;
        });
      },

      controller: [
        '$scope',
        'page',
      function(
        $scope,
        page
      ) {
        // initialize the $scope breadcrumbs from the service
        $scope.breadcrumbs = page.breadcrumbsGet();
      }]
    };
  }]);

});
