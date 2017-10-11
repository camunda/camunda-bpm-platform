'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/time-to-live.html', 'utf8');

module.exports = ['camAPI', '$window' , 'Notifications', function(camAPI, $window, Notifications) {
  return {
    restrict: 'A',
    template: template,
    scope: {
      definition: '=timeToLive',
      customOnChange: '=onChange',
      resource: '@'
    },
    link: function($scope) {
      var lastValue = getAndCorrectTimeToLiveValue();
      var resource = camAPI.resource($scope.resource);

      function customOnChange() {
        if(typeof $scope.customOnChange === 'function' ) {
          $scope.customOnChange();
        }
      }

      $scope.onChange = function() {
        $window.setTimeout(function() {
          var timeToLive = getAndCorrectTimeToLiveValue();

          updateValue(timeToLive)
            .then(customOnChange);
        });
      };

      $scope.onRemove = function() {
        updateValue(null)
          .then(function() {
            lastValue = null;
            $scope.definition.historyTimeToLive = null;
            customOnChange();
          });
      };

      $scope.format = function(property) {
        if (property === 1) {
          return property + ' day';
        }

        return property + ' days';
      };

      function updateValue(timeToLive) {
        return resource.updateHistoryTimeToLive(
          $scope.definition.id,
          {
            historyTimeToLive: timeToLive
          }
        ).catch(function(error) {
          $scope.definition.historyTimeToLive = lastValue;

          Notifications.addError({
            status: 'Failed to update history time to live',
            message: error
          });
        })
        .then(function() {
          lastValue = getAndCorrectTimeToLiveValue();
        });
      }

      function getAndCorrectTimeToLiveValue() {
        if ($scope.definition.historyTimeToLive === null) {
          return null;
        }

        return +$scope.definition.historyTimeToLive;
      }
    }
  };
}];
