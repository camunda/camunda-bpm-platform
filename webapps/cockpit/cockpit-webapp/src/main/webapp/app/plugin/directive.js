'use strict';

/* Plugin Services */

define([ "angular", "jquery"], function(angular, $) {

  var module = angular.module("cockpit.plugin");

  module.directive('plugin', function() {
    return {
      restrict: 'E', 
      scope: {
        plugin: "=ref", 
        vars: "=vars"
      }, 
      controller: 'plugin.controller', 
      transclude: true, 
      template: '<div ng-include="plugin.url"></div>', 
      link: function(scope, element, attrs) {
        
        if (!scope.vars) {
          scope.vars = {};
          console.log("[warn] not exporting any variables");
        }
        
        function exportVars(sourceScope, targetScope, exportedVariables) {
          angular.forEach(exportedVariables, function(e) {
            console.log("Export ", e, sourceScope.$id, " -> ", targetScope.$id);
            
            sourceScope.$watch(e, function(newValue, oldValue) {
              console.log("scope var change", e, ":", oldValue, " -> ", newValue, "propagate: ", sourceScope.$id, " -> ", targetScope.$id);
              if (newValue !== undefined) {
                targetScope[e] = newValue;
              }
            });
          });
        }

        scope.$on('$includeContentLoaded', function(event) {
          
          var vars = scope.vars, 
              varsRead = vars.read || [], 
              varsWrite = vars.write || [], 
              targetScope = event.targetScope;
          
          // establish binding from outer scope to include scope
          exportVars(targetScope.$parent.$parent, targetScope, varsRead);
          
          // establish binding from include scope to controller scope
          exportVars(targetScope, targetScope.$parent, varsWrite);
          exportVars(targetScope, targetScope.$parent, varsRead);
          
          // establish binding from include scope to outer scope
          exportVars(targetScope, targetScope.$parent.$parent, varsWrite);
        });
      }
    };
  });

  return module;
});