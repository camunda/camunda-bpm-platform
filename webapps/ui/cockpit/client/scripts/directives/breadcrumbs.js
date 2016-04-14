'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/breadcrumbs.html', 'utf8');

  module.exports = [
    '$location',
    'routeUtil',
    'page',
  function (
    $location,
    routeUtil,
    page
  ) {
    return {
      scope: {
        divider: '@'
      },

      restrict: 'A',

      template: template,

      link: function(scope) {
        // event triggered by the breadcrumbs service when the breadcrumbs are alterated
        scope.$on('page.breadcrumbs.changed', function(ev, breadcrumbs) {
          scope.breadcrumbs = breadcrumbs;
        });

        scope.getHref = function (crumb) {
          return routeUtil.redirectTo(crumb.href, $location.search(), crumb.keepSearchParams);
        };

        scope.getActiveChoice = function (choices) {
          var label;
          choices.forEach(function (choice) {
            if (choice.active) {
              label = choice.label;
            }
          });
          return label || 'Options';
        };
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
  }];
