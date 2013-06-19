'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function (IncidentResource) {
    return {
      restrict: 'AC',
      require: 'processDiagram',
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch('processInstanceId', function(newValue) {
          if (newValue) {
            annotateProcessDiagram(newValue);
          }
        });
        
        function annotateProcessDiagram(processInstanceId) {
          IncidentResource
          .query(
              {
                id : processInstanceId,
              })
              .$then(function(data) {
                var activities = {};
                angular.forEach(data.data, function (incident) {
                  var activity = activities[incident.activityId];
                  if (!activity) {
                    activity = [];
                    activities[incident.activityId] = activity;
                  }
                  activity.push(incident);
                });
                
                var result = [];
                for (var key in activities) {
                  var tmp = {};
                  tmp.id = key;
                  tmp.incidents = activities[key];
                  result.push(tmp);
                }
                
                processDiagram.annotateWithIncidents(result);
              });
        };
        
      }
    };
  };
  
  Directive.$inject = [ 'IncidentResource' ];
  
  module
    .directive('processInstanceIncidents', Directive);
  
});
