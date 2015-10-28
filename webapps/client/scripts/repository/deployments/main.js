define([
  'angular',

  /* controllers */
  './controllers/cam-cockpit-deployments-ctrl',

  /* directives */
  './directives/cam-cockpit-deployments',
  './directives/cam-cockpit-deployment',
  './directives/cam-cockpit-deployments-sorting-choices',

  /* plugins */
  './plugins/search/cam-cockpit-deployments-search-plugin',
  './plugins/actions/delete/cam-cockpit-delete-deployment-plugin',

  /* modals */
  './plugins/actions/delete/modals/cam-cockpit-delete-deployment-modal-ctrl'

], function(
  angular,

  /* controller */
  camCockpitDeploymentsCtrl,

  /* directives */
  camCockpitDeployments,
  camCockpitDeployment,
  camCockpitDeploymentsSortingChoices,

  /* plugins */
  camCockpitDeploymentsSearchPlugin,
  camCockpitDeleteDeploymentPlugin,

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
  deploymentsModule.directive('camDeployment', camCockpitDeployment);
  deploymentsModule.directive('camDeploymentsSortingChoices', camCockpitDeploymentsSortingChoices);

  /* plugins */
  deploymentsModule.config(camCockpitDeploymentsSearchPlugin);
  deploymentsModule.config(camCockpitDeleteDeploymentPlugin);

  /* modals */
  deploymentsModule.controller('camDeleteDeploymentModalCtrl', camCockpitDeleteDeploymentModalCtrl);

  return deploymentsModule;
});
