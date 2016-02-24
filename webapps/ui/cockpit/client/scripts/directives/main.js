'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),

  breadcrumbs = require('./breadcrumbs'),
  numeric = require('./numeric'),
  date = require('./date'),
  processDiagram = require('./processDiagram'),
  decisionTable = require('./decisionTable'),
  processDiagramPreview = require('./processDiagramPreview'),
  activityInstanceTree = require('./activityInstanceTree'),
  sidebarContainer = require('./sidebarContainer'),
  stateCircle = require('./stateCircle'),
  variable = require('./variable'),
  focus = require('./focus'),
  viewPills = require('./viewPills'),
  selectActivity = require('./selectActivity'),
  selectActivityInstance = require('./selectActivityInstance'),
  processVariable = require('./processVariable'),
  dynamicName = require('./dynamicName'),
  quickFilter = require('./quickFilter');

  var directivesModule = angular.module('cam.cockpit.directives', []);

  directivesModule.directive('camBreadcrumbsPanel', breadcrumbs);
  directivesModule.directive('numeric', numeric);
  directivesModule.directive('date', date);
  directivesModule.directive('processDiagram', processDiagram);
  directivesModule.directive('decisionTable', decisionTable);
  directivesModule.directive('processDiagramPreview', processDiagramPreview);
  directivesModule.directive('activityInstanceTree', activityInstanceTree);
  directivesModule.directive('ctnCollapsableParent', sidebarContainer);
  directivesModule.directive('stateCircle', stateCircle);
  directivesModule.directive('variable', variable);
  directivesModule.directive('focus', focus);
  directivesModule.directive('viewPills', viewPills);
  directivesModule.directive('camSelectActivity', selectActivity);
  directivesModule.directive('camSelectActivityInstance', selectActivityInstance);
  directivesModule.directive('processVariable', processVariable);
  directivesModule.directive('camDynamicName', dynamicName);
  directivesModule.directive('camQuickFilter', quickFilter);

  module.exports = directivesModule;
