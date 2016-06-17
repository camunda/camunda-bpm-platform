'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camTasklistForm = require('./directives/cam-tasklist-form'),
    camTasklistFormGeneric = require('./directives/cam-tasklist-form-generic'),
    camTasklistFormGenericVariables = require('./directives/cam-tasklist-form-generic-variables'),
    camTasklistFormEmbedded = require('./directives/cam-tasklist-form-embedded'),
    camTasklistFormExternal = require('./directives/cam-tasklist-form-external'),
    camTasklistUniqueValue = require('./directives/cam-tasklist-unique-value');

var formModule = angular.module('cam.tasklist.form', [
  'ui.bootstrap'
]);

formModule.directive('camTasklistForm', camTasklistForm);
formModule.directive('camTasklistFormGeneric', camTasklistFormGeneric);
formModule.directive('camTasklistFormGenericVariables', camTasklistFormGenericVariables);
formModule.directive('camTasklistFormEmbedded', camTasklistFormEmbedded);
formModule.directive('camTasklistFormExternal', camTasklistFormExternal);
formModule.directive('camUniqueValue', camTasklistUniqueValue);

module.exports = formModule;
