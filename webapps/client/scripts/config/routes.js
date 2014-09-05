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
        // controller: [
        //   '$rootScope',
        //   '$location',
        // function(
        //   $rootScope,
        //   $location
        // ) {
        //   if ($rootScope.authentication) {
        //     // $location.replace()
        //   }
        // }],
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
