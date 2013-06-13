'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function (ProcessDefinitionActivityStatisticsResource) {
    return {
      restrict: 'AC',
      require: 'processDiagram', 
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch(attrs['processDefinitionId'], function (newValue) {
          processDiagram.annotateWithActivityStatistics(null);
          getActivityStatistics(newValue);
        });
        
        function getActivityStatistics(processDefinitionId) {
          ProcessDefinitionActivityStatisticsResource
          .queryStatistics(
              {
                id : processDefinitionId
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
