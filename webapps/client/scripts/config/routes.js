define([
  'text!camunda-tasklist-ui/index.html'
], function(
  tasklistTemplate
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
        authentication: 'required',
        reloadOnSearch: false
      })


      .when('/login', {
        template: tasklistTemplate,
        controller: 'userLoginCtrl'
      })


      .when('/logout', {
        template: tasklistTemplate,
        controller: 'userLogoutCtrl'
      })


      .otherwise({
        redirectTo: '/'
      })
    ;
  }];
});
