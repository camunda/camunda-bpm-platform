'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function () {
    return {
      restrict: 'AC',
      require: 'processDiagram',
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch(attrs['activityInstances'], function(newValue) {
          if (newValue) {
            annotateProcessDiagram(newValue);
          }
        });
        
        function annotateProcessDiagram(tree) {
          processDiagram.annotateWithActivityInstances(tree);
        };
        
      }
    };
  };
  
  module
    .directive('activityInstances', Directive);
  
});
