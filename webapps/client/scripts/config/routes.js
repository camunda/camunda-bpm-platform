define([
  'text!camunda-tasklist-ui/index.html',
  'text!camunda-tasklist-ui/user/controller/cam-auth-login.html'
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
