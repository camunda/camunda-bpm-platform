  'use strict';

  module.exports = function() {
    return {
      link: function(scope, element) {

        var processData = scope.processData;
        var selectedActivityInstanceQuery = element.attr('cam-select-activity-instance');

        if (!processData) {
          throw new Error('No processData defined in scope');
        }

        if (!selectedActivityInstanceQuery) {
          throw new Error('No activity instance id query given in @cam-select-activity');
        }

        element.on('click', function(event) {

          event.preventDefault();

          scope.$apply(function() {
            // refresh view with selected activity instance id
            processData.set('filter', {
              activityInstanceIds: [scope.$eval(selectedActivityInstanceQuery)]
            });
          });
        });
      }
    };
  };
