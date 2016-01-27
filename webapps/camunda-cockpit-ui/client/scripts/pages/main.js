'use strict';

var angular = require('angular'),

    dashboard = require('./dashboard'),
    processDefinitionModule = require('./processDefinition'),
    processInstanceModule = require('./processInstance'),
    decisionDefinitionModule = require('./decisionDefinition'),
    decisionInstanceModule = require('./decisionInstance');

  var pagesModule = angular.module('cam.cockpit.pages', [processDefinitionModule.name, processInstanceModule.name, decisionDefinitionModule.name, decisionInstanceModule.name]);

  pagesModule.config(dashboard);

  module.exports = pagesModule;
