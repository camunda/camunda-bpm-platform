  'use strict';

  module.exports = function() {
    return {
      link: function(scope, element) {

        var processData = scope.processData;
        var selectedActivityQuery = element.attr('cam-select-activity');

        if (!processData) {
          throw new Error('No processData defined in scope');
        }

        if (!selectedActivityQuery) {
          throw new Error('No activity ID query given in @cam-select-activity');
        }

        element.on('click', function(event) {

          event.preventDefault();

          scope.$apply(function() {
            // refresh view with selected activity instance id
            processData.set('filter', {
              activityIds: [scope.$eval(selectedActivityQuery)]
            });
          });
        });
      }
    };
  };
