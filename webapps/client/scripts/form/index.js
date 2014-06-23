'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'angular-moment',
           'text!camunda-tasklist-ui/form/inline-field.html',
           'text!camunda-tasklist-ui/form/form.html'
], function(angular) {

  /**
   * @module cam.form
   */

  var formModule = angular.module('cam.form', [
    'angularMoment'
  ]);
  var c = 1000;



  formModule.directive('camFormInlineField', function() {
    return {
      scope: {
        'value': '='
      },
      link: function(scope) {
        scope.editing = false;
        scope.toggleEditing = function() {
          scope.editing = !scope.editing;
        };

        scope.applyChange = function() {
          // should
          // - validate
          // - update the value
          scope.editing = false;
        };

        scope.cancelChange = function() {
          scope.editing = false;
        };
      },

      transclude: true,

      template: require('text!camunda-tasklist-ui/form/inline-field.html')
    };
  });



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

