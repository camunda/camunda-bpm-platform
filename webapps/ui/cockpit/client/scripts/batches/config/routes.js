'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/../templates/cam-cockpit-batch-view.html', 'utf8');
var ctrl = require('../controllers/view-ctrl');

module.exports = [
  '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
      .when('/batch', {
        template: template,
        controller: ctrl,
        authentication: 'required',
        reloadOnSearch: false
      });
  }];
