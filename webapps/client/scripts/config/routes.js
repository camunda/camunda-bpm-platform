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
        authentication: 'required'
      })

      // // Would be great to be able to start processes with a URL
      // .when('/process/:processDefinitionId/start', {
      //   template: tasklistTemplate,
      //   controller: 'processStartCtrl'
      // })
      // .when('/process/key/:processDefinitionKey/start', {
      //   template: tasklistTemplate,
      //   controller: 'processStartCtrl'
      // })


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
