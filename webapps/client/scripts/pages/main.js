define([
  'angular',

  './dashboard',
  './processDefinition',
  './processInstance',
  './decisionDefinition'

], function(
  angular,

   dashboard,
   processDefinitionModule,
   processInstanceModule,
   decisionDefinitionModule
) {

  'use strict';

  var pagesModule = angular.module('cam.cockpit.pages', [processDefinitionModule.name, processInstanceModule.name, decisionDefinitionModule.name]);

  pagesModule.config(dashboard);

  return pagesModule;

});
