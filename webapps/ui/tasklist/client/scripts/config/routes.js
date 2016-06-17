'use strict';
var fs = require('fs');

var tasklistTemplate = fs.readFileSync(__dirname + '/../index.html', 'utf8');
var userLoginTemplate = fs.readFileSync(__dirname + '/../user/controller/cam-auth-login.html', 'utf8');

require('./../user/index');

module.exports = [
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
