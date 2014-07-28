'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }

define([
  'angular',
  'camunda-tasklist-ui/utils',
  'camunda-commons-ui/util/index',
  'text!camunda-tasklist-ui/navigation/navigation.html',
], function(
  angular
) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.directive('camTasklistNavigation', function() {
    return {
      scope: {},

      template: require('text!camunda-tasklist-ui/navigation/navigation.html'),

      controller: [
        '$rootScope',
        '$scope',
        'Uri',
      function(
        $rootScope,
        $scope,
        Uri
      ) {
        var auth;

        function reset() {
          $scope.links = [
            {
              title: '',
              icon: 'home',
              links: []
            }
          ];

          $scope.username = null;
        }


        function refresh() {
          auth = $rootScope.authentication;

          if (!auth || !auth.user) {
            reset();
            return;
          }

          if ($scope.username === auth.username()) {
            return;
          }

          reset();

          $scope.username = auth.username();

          var appLinks = {
            admin: {
              title: 'Admin',
              href: Uri.appUri('adminbase://:engine/#')
            },
            cockpit: {
              title: 'Cockpit',
              href: Uri.appUri('cockpitbase://:engine/#')
            }
          };

          angular.forEach(appLinks, function(info, appName) {
            if (auth.user.authorizedApps.indexOf(appName) > -1) {
              $scope.links[0].links.push(info);
            }
          });
        }

        // $rootScope.$on('loggedin', refresh);
        // $rootScope.$on('loggedout', refresh);

        // initializes...
        refresh();
      }]
    };
  });

  return navigationModule;
});
