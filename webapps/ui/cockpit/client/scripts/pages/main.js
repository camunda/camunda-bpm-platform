'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

    dashboard = require('./dashboard'),
    processes = require('./processes'),
    decisions = require('./decisions'),
    tasks = require('./tasks'),
    processDefinitionModule = require('./processDefinition'),
    processInstanceModule = require('./processInstance'),
    decisionDefinitionModule = require('./decisionDefinition'),
    decisionInstanceModule = require('./decisionInstance');

var pagesModule = angular.module('cam.cockpit.pages', [
  processDefinitionModule.name,
  processInstanceModule.name,
  decisionDefinitionModule.name,
  decisionInstanceModule.name
]);

pagesModule.config(dashboard);
pagesModule.config(processes);
pagesModule.config(decisions);
pagesModule.config(tasks);

module.exports = pagesModule;
