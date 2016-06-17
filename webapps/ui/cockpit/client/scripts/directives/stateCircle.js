  'use strict';

  module.exports = function() {
    return {
      restrict: 'EAC',
      link: function(scope, element, attrs) {
        element.addClass('circle');

        scope.$watch(attrs.incidents, function() {
          updateStateCircle();
        });

        function updateStateCircle() {
          var incidents = scope.$eval(attrs.incidents);
          var incidentsForTypes = scope.$eval(attrs.incidentsForTypes) ||  [];

          if (!!incidents && incidents.length > 0) {

            // In that case 'incidentsForTypes.length === 0' means
            // that the state has to be set to red independent
            // from the incident type.
            // Note: incidents.length is greater than zero.
            if (incidentsForTypes.length === 0) {
              setStateToRed();
              return;
            }

            // In the other case we check whether there exist
            // at least one incident to one of the incident types.
            for(var i = 0; i < incidents.length; i++) {
              var incident = incidents[i];

              if(incident.incidentType.indexOf(incidentsForTypes) != -1) {
                if (incident.incidentCount > 0) {
                  setStateToRed();
                  return;
                }
              }

            }
          }
          // If there does not exist any incident, the state is green.
          setStateToGreen();
        }

        function setStateToGreen() {
          element.removeClass('circle-red').addClass('circle-green');
        }

        function setStateToRed() {
          element.removeClass('circle-green').addClass('circle-red');
        }
      }
    };
  };
