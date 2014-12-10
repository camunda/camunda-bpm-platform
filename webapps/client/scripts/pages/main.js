define([
  'angular',

  './dashboard',
  './processDefinition',
  './processInstance'

], function(
  angular,

   dashboard,
   processDefinitionModule,
   processInstanceModule
) {

  'use strict';

  var pagesModule = angular.module('cam.cockpit.pages', [processDefinitionModule.name, processInstanceModule.name]);

  pagesModule.config(dashboard);

  return pagesModule;

});
