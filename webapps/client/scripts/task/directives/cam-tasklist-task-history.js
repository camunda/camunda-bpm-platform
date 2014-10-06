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
  return ['camAPI',
  function(camAPI) {
    var History = camAPI.resource('history');
    return {
      scope: {
        task : '='
      },
      link: function($scope) {
        $scope.history = [];
        $scope.days = [];
        var loadHistory = function(taskId) {
          History.userOperation({
            taskId : taskId
          }, function(err, historyData) {
            $scope.history = historyData;
            var days = {};
            var daysArray = [];
            angular.forEach($scope.history, function(event) {
              var mom = moment(event.timestamp, 'YYYY-MM-DDTHH:mm:ss');
              var date = mom.format('DD-MMMM-YYYY');
              var time = mom.format('HH:mm');
              var parts = date.split('-');

              // create object for each day, containing the events for this day
              if(!days[date]) {
                days[date] = {
                  date: {
                    day: parts[0],
                    monthWord: parts[1],
                    month: mom.format('MM'),
                    year: parts[2]
                  },
                  events: {},
                  eventArray: []
                };
                daysArray.push(days[date]);
              }
              
              // process formatting of time
              switch(event.property) {
                case 'dueDate':
                case 'followUpDate':
                  if(event.orgValue) {
                    event.orgValue = moment(event.orgValue / 1000, 'X').format('DD. MMMM YYYY HH:mm');
                  }
                  event.newValue = moment(event.newValue / 1000, 'X').format('DD. MMMM YYYY HH:mm');
                  break;
              }

              // create event object for each operationId
              if(!days[date].events[event.operationId]) {
                days[date].events[event.operationId] = {
                  time: time, 
                  timestamp: event.timestamp,
                  type: event.operationType,
                  userId: event.userId, 
                  subEvents: []
                };
                days[date].eventArray.push(days[date].events[event.operationId]);
              }
              days[date].events[event.operationId].subEvents.push(event);
            });
            $scope.days = daysArray;
          });
        };
        loadHistory($scope.task.id);
        $scope.$on('tasklist.task.current', function(evt) {
          loadHistory(evt.targetScope.currentTask.id);
        });
        $scope.$on('tasklist.task.update', function(evt) {
          loadHistory(evt.targetScope.currentTask.id);
        });
        
        
      },
      template: template
    };
  }];
});
