"use strict";

define([ "angular", "jquery" ], function(angular, $) {
  
  var module = angular.module("common.directives");
  
  var errorPanelTpl =
    '<div class="errorPanel">' +
    '  <div ng-repeat="error in errors" class="alert alert-error">' +
    '    <button type="button" class="close" ng-click="removeError(error)">&times;</button>' +
    '    <strong>{{ error.status }}:</strong> <span ng-bind-html="error.message"></span>' +
    '  </div>' +
    '</div>';
  
  var Directive = function (Errors, Uri) {
    return {
      scope: true,
      template: errorPanelTpl,
      link: function(scope, element, attrs, $destroy) {

        var errors = scope.errors = scope.errors || [];

        var consumer = {
          add: function(error) {
            errors.push(error);
          },
          remove: function(error) {
            var idx = errors.indexOf(error);
            if (idx != -1) {
              errors.splice(idx, 1);
            }
          }
        };

        Errors.registerConsumer(consumer);

        scope.removeError = function(error) {
          errors.splice(errors.indexOf(error), 1);
        };

        scope.$on($destroy, function() {
          Errors.registerConsumer(consumer);
        });
      }
    };
  };

  Directive.$inject = ["Errors", "Uri"];
  
  module
    .directive("errorPanel", Directive);

});
