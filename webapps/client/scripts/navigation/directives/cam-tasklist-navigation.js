define([
  'text!./cam-tasklist-navigation.html'
], function(
  template
) {
  'use strict';
  return function() {
    return {

      template: template,

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

          if (!auth) {
            return reset();
          }

          if ($scope.username === auth.name) {
            return;
          }

          reset();

          $scope.username = auth.name;

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
            if (auth.canAccess(appName)) {
              $scope.links[0].links.push(info);
            }
          });
        }

        $rootScope.$on('authentication.login.success', refresh);
        $rootScope.$on('authentication.logout.success', refresh);

        // initializes...
        refresh();
      }]
    };
  };
});
