'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define(['angular', 'camunda-tasklist-ui/utils', 'text!camunda-tasklist-ui/navigation/navigation.html', 'camunda-tasklist-ui/session'],
function(angular) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-tasklist-ui/utils').name,
    'ui.bootstrap',
    'cam.tasklist.user',
    'cam.tasklist.session'
  ]);

  navigationModule.directive('camTasklistNavigation', function() {
    return {
      scope: {},
      controller: [
              '$rootScope', '$scope', '$modal', 'camStorage', 'camSettings', 'camLegacySessionData', 'camTasklistNotifier',
      function($rootScope,   $scope,   $modal,   camStorage,   camSettings,   camLegacySessionData,   camTasklistNotifier) {
        var settings = camSettings(navigationModule);
        var modalInstance;
        var prevUserId;

        function login(silent) {
          camLegacySessionData.retrieve()
          .then(function(data) {
            $rootScope.user = data;

            $rootScope.user.id = $rootScope.user.id || data.userId;

            camStorage.set('user', $rootScope.user);

            if (modalInstance) {
              modalInstance.close();
            }
          }, function(err) {
            console.info('nopeeee....', err, silent);
            if (!silent) {
              camTasklistNotifier.add({
                type: 'error',
                text: 'You can not log in with these credentials.'
              });
            }

            camStorage.remove('user');
            $rootScope.user = {};

            modalInstance = $modal.open({
              backdrop:     false,
              keyboard:     false,
              windowClass:  'user-login',
              template:     require('text!camunda-tasklist-ui/user/login.html')
            });
          });
        }

        $scope.links = settings.links || [];

        $scope.user = $rootScope.user;

        $rootScope.$watch('user', function() {
          if ((''+ $rootScope.user.id) === (''+ prevUserId)) {
            return;
          }

          prevUserId = $rootScope.user.id;
          $scope.user = $rootScope.user;

          if (prevUserId) {
            if (modalInstance) {
              modalInstance.close();
              modalInstance = null;
            }
          }
          else if(!modalInstance) {
            login();
          }
        });

        if (!$scope.user) {
          $rootScope.user = camStorage.get('user') || {};
        }

        if (!$rootScope.user.id && !modalInstance) {
          login(true);
        }
      }],
      template: require('text!camunda-tasklist-ui/navigation/navigation.html')
    };
  });

  return navigationModule;
});
