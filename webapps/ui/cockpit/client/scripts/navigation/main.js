'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camHeaderViewsCtrl = require('./controllers/cam-header-views-ctrl');

var navigationModule = angular.module('cam.cockpit.navigation', []);

navigationModule.controller('camHeaderViewsCtrl', camHeaderViewsCtrl);

module.exports = navigationModule;
