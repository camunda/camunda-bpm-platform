define([
  'angular',

  './breadcrumbs',
  './numeric',
  './date',
  './processDiagram',
  './decisionTable',
  './processDiagramPreview',
  './activityInstanceTree',
  './sidebarContainer',
  './stateCircle',
  './variable',
  './focus',
  './viewPills',
  './selectActivity',
  './selectActivityInstance',
  './processVariable',
  './dynamicName',
  './quickFilter'
], function(
  angular,

  breadcrumbs,
  numeric,
  date,
  processDiagram,
  decisionTable,
  processDiagramPreview,
  activityInstanceTree,
  sidebarContainer,
  stateCircle,
  variable,
  focus,
  viewPills,
  selectActivity,
  selectActivityInstance,
  processVariable,
  dynamicName,
  quickFilter
) {

  'use strict';

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

  return directivesModule;

});
