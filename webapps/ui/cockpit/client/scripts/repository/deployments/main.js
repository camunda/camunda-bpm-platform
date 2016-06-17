'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

    /* controller */
    camCockpitDeploymentsCtrl = require('./controllers/cam-cockpit-deployments-ctrl'),

    /* directives */
    camCockpitDeployments = require('./directives/cam-cockpit-deployments'),
    camCockpitDeployment = require('./directives/cam-cockpit-deployment'),
    camCockpitDeploymentsSortingChoices = require('./directives/cam-cockpit-deployments-sorting-choices'),

    /* plugins */
    camCockpitDeploymentsSearchPlugin = require('./plugins/search/cam-cockpit-deployments-search-plugin'),
    camCockpitDeleteDeploymentPlugin = require('./plugins/actions/delete/cam-cockpit-delete-deployment-plugin'),

    /* modals */
    camCockpitDeleteDeploymentModalCtrl = require('./plugins/actions/delete/modals/cam-cockpit-delete-deployment-modal-ctrl');

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

module.exports = deploymentsModule;
