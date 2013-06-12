'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function (ProcessDefinitionActivityStatisticsResource) {
    return {
      restrict: 'AC',
      require: 'processDiagram', 
      link: function(scope, element, attr, processDiagram) {
        
        scope.$watch('processDefinitionId', function (newValue) {
          processDiagram.annotateWithActivityStatistics(null);
          getActivityStatistics();
        });
        
        function getActivityStatistics() {
          ProcessDefinitionActivityStatisticsResource
          .queryStatistics(
              {
                id : scope.processDefinitionId
              })
              .$then(function(result) {
                processDiagram.annotateWithActivityStatistics(result.data);
              });
        }
      }
    };
  };
  
  Directive.$inject = [ 'ProcessDefinitionActivityStatisticsResource' ];
   
  module
    .directive('activityStatistics', Directive);
  
});
