'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/breadcrumbs.html', 'utf8');

module.exports = [
  '$location',
  'routeUtil',
  function(
    $location,
    routeUtil
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

        scope.getHref = function(crumb) {
          return routeUtil.redirectTo(crumb.href, $location.search(), crumb.keepSearchParams);
        };

        scope.selectChoice = function(evt, choice) {
          evt.preventDefault();
          $location.path(choice.href.substr(1));
        };

        scope.getActiveChoice = function(choices) {
          var label;
          choices.forEach(function(choice) {
            if (choice.active) {
              label = choice.label;
            }
          });
          return label || 'Options';
        };

        scope.sortedChoices = function(choices) {
          return choices.sort(function(a, b) {
            return a.active ? -1 : (b.active ? 1 : 0);
          });
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
