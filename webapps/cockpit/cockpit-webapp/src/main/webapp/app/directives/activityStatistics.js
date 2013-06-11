'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function (ProcessDefinitionActivityStatisticsResource) {
    return {
      restrict: 'A',
      require: 'processDiagram', 
      link: function(scope, element, attr, processDiagram) {
        
        scope.$watch(function() { return processDiagram.getRenderer(); }, function (newValue) {
          if (!!newValue) {
            getActivityStatisics();
          }
        });

        function getActivityStatisics () {
          ProcessDefinitionActivityStatisticsResource
            .queryStatistics(
              {
                id : scope.processDefinitionId
              })
              .$then(function(result) {
                processDiagram.annotateWithActivityStatistics(result.data);
          });
        };
        
      }
    };
  };
  
  Directive.$inject = [ 'ProcessDefinitionActivityStatisticsResource' ];
   
  module
    .directive('activityStatistics', Directive);
  
});
