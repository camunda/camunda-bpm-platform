define([
  'angular',
  './directives/cam-widget-inline-field',
  'angular-moment',
], function(
  angular,
  inlineField
) {
  'use strict';

  /**
   * @module cam.widgets
   */

  var formModule = angular.module('cam.widget', [
    'angularMoment'
  ]);

  formModule.directive('camWidgetInlineField', inlineField);

  return formModule;
});

