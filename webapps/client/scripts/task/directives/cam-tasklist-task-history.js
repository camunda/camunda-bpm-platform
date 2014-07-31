define([
  'angular',
  'moment',

  'text!./cam-tasklist-task-history.html'
], function(
  angular,
  moment,

  template
) {
  'use strict';
  return [function() {
    return {
      link: function($scope) {
        $scope.history = [];
        $scope.days = [];

        $scope.$on('tasklist.task.current', function() {
          // scope.history = camTaskHistoryData(null, null);
          $scope.history = [];
          $scope.now = new Date();
          var days = {};
          angular.forEach($scope.history, function(event) {
            var mom = moment(event.timestamp, 'X');
            var date = mom.format('DD-MMMM-YYYY');
            var time = mom.format('HH:mm');
            var parts = date.split('-');

            days[date] = days[date] || {
              date: {
                day: parts[0],
                month: parts[1],
                year: parts[2]
              },
              events: {}
            };
            days[date].events[time] = days[date].events[time] || [];
            days[date].events[time].push(event);
          });
          $scope.days = days;
        });
      },
      template: template
    };
  }];
});
