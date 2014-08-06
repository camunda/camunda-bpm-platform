define([
  'angular',
  './directives/cam-form-inline-field',
  'angular-moment',
  'text!camunda-tasklist-ui/form/form.html'
], function(
  angular,
  inlineField
) {
  'use strict';

  /**
   * @module cam.form
   */

  var formModule = angular.module('cam.form', [
    'angularMoment'
  ]);
  var c = 1000;



  formModule.directive('camFormInlineField', inlineField);



  formModule.directive('camForm', function() {
    return {
      link: function(scope) {
        scope.elUID = c;
        c++;

        scope.labelsWidth = 3;
        scope.fieldsWidth = 12 - scope.labelsWidth;
      },
      template: require('text!camunda-tasklist-ui/form/form.html')
    };
  });

  return formModule;
});

