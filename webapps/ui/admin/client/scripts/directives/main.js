'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

    breadcrumbs = require('./../../../../common/scripts/directives/breadcrumbs');

var directivesModule = module.exports = angular.module('cam.admin.directives', []);
directivesModule.directive('camBreadcrumbsPanel', breadcrumbs);
