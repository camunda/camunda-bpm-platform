'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'require', 'angular', 'moment', 'jquery',
           'camunda-tasklist/user/data',
           'text!camunda-tasklist/user/login.html'
], function(require,   angular,   moment,   $) {

  /**
   * @module cam.tasklist.user
   */

  /**
   * @memberof cam.tasklist
   */

  var userModule = angular.module('cam.tasklist.user', [
    'cam.tasklist.user.data',
    'ui.bootstrap',
    'cam.form'
  ]);


  userModule.controller('userLoginCtrl', [
          '$location', '$modal', '$scope', '$rootScope',
  function($location,   $modal,   $scope,   $rootScope) {
    $modal.open({
      windowClass:  'user-login',
      template:     require('text!camunda-tasklist/user/login.html')
    });
  }]);


  userModule.controller('userLoginModalFormCtrl', [
          '$scope', '$rootScope', '$location', 'camStorage', 'camLegacySessionData', 'camTasklistNotifier',
  function($scope,   $rootScope,   $location,   camStorage,   camLegacySessionData,   camTasklistNotifier) {
    $rootScope.$watch('user', function() {
      $scope.user = $rootScope.user;
    });

    $scope.submitForm = function(htmlForm) {
      return htmlForm.$valid;
    };

    // /camunda/api/admin/auth/user/default/login/cockpit
    $scope.ok = function() {
      camLegacySessionData.create($scope.username, $scope.password)

      .then(function(data) {
        $rootScope.user = data;
        $rootScope.user.id = $rootScope.user.id || data.userId;
        camStorage.set('user', $rootScope.user);
      }, function(err) {
        $rootScope.user = {};

        camStorage.remove('user');

        camTasklistNotifier.add({
          type: 'error',
          text: 'Can not log in with these credentials.'
        });
      });
    };


    $scope.cancel = function() {
      camStorage.remove('user');

      $rootScope.user = {};

      $scope.$parent.$parent.$dismiss();
    };
  }]);


  userModule.controller('userLogoutCtrl', [
          '$location', '$rootScope', 'camStorage', 'camLegacySessionData',
  function($location,   $rootScope,   camStorage,   camLegacySessionData) {
    function logout() {
      camStorage.remove('user');

      $rootScope.user = {};

      $location.path('/loggedout');
    }

    camLegacySessionData.destroy()
    .then(logout, logout);
  }]);

  return userModule;
});
