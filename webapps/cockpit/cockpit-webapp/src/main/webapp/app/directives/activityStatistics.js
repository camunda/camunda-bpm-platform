'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {

  var Directive = [ 'ProcessDefinitionResource', function (ProcessDefinitionResource) {
    return {
      restrict: 'AC',
      require: 'processDiagram',
      link: function(scope, element, attrs, processDiagram) {

        scope.$watch(attrs['processDefinitionId'], function (newValue) {
          processDiagram.annotateWithActivityStatistics(null);
          getActivityStatistics(newValue);
        });

        function getActivityStatistics(processDefinitionId) {
          ProcessDefinitionResource
            .queryActivityStatistics(
                {
                  id : processDefinitionId
                })
                .$then(function(result) {
                  processDiagram.annotateWithActivityStatistics(result.data);
                });
        }
      }
    };
  }];

  module
    .directive('activityStatistics', Directive);

});
