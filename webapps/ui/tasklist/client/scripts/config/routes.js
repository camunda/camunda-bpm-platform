'use strict';
var fs = require('fs');

var tasklistTemplate = fs.readFileSync(__dirname + '/../index.html', 'utf8');

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
      .otherwise({
        redirectTo: '/'
      });
  }];
