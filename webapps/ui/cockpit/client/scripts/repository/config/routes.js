'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/../controllers/cam-cockpit-repository-view.html', 'utf8');

module.exports = [
  '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
      .when('/repository', {
        template: template,
        controller: 'camCockpitRepositoryViewCtrl',
        authentication: 'required',
        reloadOnSearch: false
      });
  }];
