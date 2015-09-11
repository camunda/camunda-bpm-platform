define([
  'angular',

  './dashboard',
  './processDefinition',
  './processInstance',
  './decisionDefinition',
  './decisionInstance'

], function(
  angular,

   dashboard,
   processDefinitionModule,
   processInstanceModule,
   decisionDefinitionModule,
   decisionInstanceModule
) {

  'use strict';

  var pagesModule = angular.module('cam.cockpit.pages', [processDefinitionModule.name, processInstanceModule.name, decisionDefinitionModule.name, decisionInstanceModule.name]);

  pagesModule.config(dashboard);

  return pagesModule;

});
