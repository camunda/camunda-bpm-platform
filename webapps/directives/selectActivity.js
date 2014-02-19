/* global ngDefine: true */

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {

  'use strict';

  module.directive('camSelectActivity', function() {
    return {
      link: function(scope, element, attrs) {

        var processData = scope.processData;
        var selectedActivityQuery = element.attr('cam-select-activity');

        if (!processData) {
          throw new Error('No processData defined in scope');
        }

        if (!selectedActivityQuery) {
          throw new Error('No activity id query given in @cam-select-activity');
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
  });

  module.directive('camSelectActivityInstance', function() {
    return {
      link: function(scope, element, attrs) {

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
  });

});