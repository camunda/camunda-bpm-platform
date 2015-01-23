define([
  'text!./../index.html',
  'text!./../user/controller/cam-auth-login.html',
  './../user/index'
], function(
  tasklistTemplate,
  userLoginTemplate
) {
  'use strict';

  return [
    '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
      .when('/', {
        template: tasklistTemplate,
        controller: 'camTasklistViewCtrl',
        authentication: 'required',
        reloadOnSearch: false
      })

      .when('/login', {
        template: userLoginTemplate,
        controller: 'camUserLoginCtrl'
      })

      .when('/logout', {
        template: userLoginTemplate,
        authentication: 'required',
        controller: 'camUserLogoutCtrl'
      })

      .otherwise({
        redirectTo: '/'
      })
    ;
  }];
});
