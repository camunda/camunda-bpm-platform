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
        template: tasklistTemplate
      })


      .when('/logout', {
        template: tasklistTemplate,
        authentication: 'required',
        controller: 'userLogoutCtrl'
      })


      .otherwise({
        redirectTo: '/'
      })
    ;
  }];
});
