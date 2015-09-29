define([
  'angular',

  /* controllers */
  './controllers/cam-cockpit-deployments-ctrl',

  /* directives */
  './directives/cam-cockpit-deployments',
  './directives/cam-cockpit-deployments-sorting-choices',

  /* plugins */
  './plugins/search/cam-cockpit-deployments-search-plugin',

  /* modals */
  './modals/cam-cockpit-delete-deployment-modal-ctrl'

], function(
  angular,

  /* controller */
  camCockpitDeploymentsCtrl,

  /* directives */
  camCockpitDeployments,
  camCockpitDeploymentsSortingChoices,

  /* plugins */
  camCockpitDeploymentsSearchPlugin,

  /* modals */
  camCockpitDeleteDeploymentModalCtrl
) {
  'use strict';

  var deploymentsModule = angular.module('cam.cockpit.repository.deployments', [
    'ui.bootstrap'
  ]);

  /* controllers */
  deploymentsModule.controller('camDeploymentsCtrl', camCockpitDeploymentsCtrl);

  /* directives */
  deploymentsModule.directive('camDeployments', camCockpitDeployments);
  deploymentsModule.directive('camDeploymentsSortingChoices', camCockpitDeploymentsSortingChoices);

  /* plugins */
  deploymentsModule.config(camCockpitDeploymentsSearchPlugin);

  /* modals */
  deploymentsModule.controller('camDeleteDeploymentModalCtrl', camCockpitDeleteDeploymentModalCtrl);

  return deploymentsModule;
});
