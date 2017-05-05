'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/time-to-live.html', 'utf8');

module.exports = ['camAPI', '$window' , 'Notifications', function(camAPI, $window, Notifications) {
  return {
    restrict: 'A',
    template: template,
    scope: {
      definition: '=timeToLive',
      resource: '@'
    },
    link: function($scope) {
      var resource = camAPI.resource($scope.resource);

      $scope.onChange = function() {
        $window.setTimeout(function() {
          var timeToLive = getAndCorrectTimeToLiveValue();

          resource.updateHistoryTimeToLive(
            $scope.definition.id,
            {
              historyTimeToLive: timeToLive
            }
          ).catch(function(error) {
            Notifications.addError({
              status: 'Failed to update history time to live',
              message: error
            });
          });
        });
      };

      $scope.format = function(property) {
        if (property === 1) {
          return property + ' day';
        }

        return property + ' days';
      };


      function getAndCorrectTimeToLiveValue() {
        if ($scope.definition.historyTimeToLive === null) {
          return null;
        }

        var timeToLive = +$scope.definition.historyTimeToLive;

        if (isNaN(timeToLive) || timeToLive < 0) {
          timeToLive = null;
          $scope.definition.historyTimeToLive = null;
        }

        return timeToLive;
      }
    }
  };
}];
