'use strict';

define([
  'angular',
  './directives/cam-tasklist-form',
  './directives/cam-tasklist-form-generic',
  './directives/cam-tasklist-form-generic-variables',
  './directives/cam-tasklist-form-embedded',
  './directives/cam-tasklist-form-external',

  './directives/cam-tasklist-unique-value'
], function(
  angular,
  camTasklistForm,
  camTasklistFormGeneric,
  camTasklistFormGenericVariables,
  camTasklistFormEmbedded,
  camTasklistFormExternal,
  camTasklistUniqueValue
) {

  var formModule = angular.module('cam.tasklist.form', [
    'ui.bootstrap'
  ]);

  formModule.directive('camTasklistForm', camTasklistForm);
  formModule.directive('camTasklistFormGeneric', camTasklistFormGeneric);
  formModule.directive('camTasklistFormGenericVariables', camTasklistFormGenericVariables);
  formModule.directive('camTasklistFormEmbedded', camTasklistFormEmbedded);
  formModule.directive('camTasklistFormExternal', camTasklistFormExternal);
  formModule.directive('camUniqueValue', camTasklistUniqueValue);

  return formModule;
});
