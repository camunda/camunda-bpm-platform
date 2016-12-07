'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camLayoutCtrl = require('./controllers/cam-layout-ctrl'),
    camHeaderViewsCtrl = require('./controllers/cam-header-views-ctrl');

require('camunda-commons-ui/lib/util/index');

var navigationModule = angular.module('cam.tasklist.navigation', [
  require('camunda-commons-ui/lib/util/index').name,
  'ui.bootstrap'
]);

navigationModule.controller('camHeaderViewsCtrl', camHeaderViewsCtrl);
navigationModule.controller('camLayoutCtrl', camLayoutCtrl);

module.exports = navigationModule;
