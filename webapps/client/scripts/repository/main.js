define([
  'angular',
  'angular-data-depend',
  'camunda-commons-ui',

  /* config */
  './config/routes',

  /* controllers */
  './controllers/cam-cockpit-repository-view-ctrl',

  /* modules */
  './deployments/main',
  './resources/main',
  './resource/main'
], function(
  angular,
  dataDepend,
  camCommons,

  routes,

  camCockpitRepositoryViewCtrl,

  deploymentsModule,
  resourcesModule,
  resourceDetailsModule
) {

  'use strict';

  var ngDeps = [
    'cam.commons',
    'dataDepend',
    'ngRoute',
    deploymentsModule.name,
    resourcesModule.name,
    resourceDetailsModule.name
  ];

  var deploymentModule = angular.module('cam.cockpit.repository', ngDeps);

  deploymentModule.config(routes);

  deploymentModule.controller('camCockpitRepositoryViewCtrl', camCockpitRepositoryViewCtrl);

  return deploymentModule;

});
