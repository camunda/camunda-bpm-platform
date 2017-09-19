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
      var lastValue = getAndCorrectTimeToLiveValue();
      var resource = camAPI.resource($scope.resource);

      $scope.onChange = function() {
        $window.setTimeout(function() {
          var timeToLive = getAndCorrectTimeToLiveValue();

          updateValue(timeToLive);
        });
      };

      $scope.onRemove = function() {
        updateValue(null)
          .then(function() {
            lastValue = null;
            $scope.definition.historyTimeToLive = null;
          });
      };

      $scope.format = function(property) {
        if (property === 1) {
          return property + ' day';
        }

        return property + ' days';
      };

      function updateValue(timeToLive) {
        var id = (
          $scope.definition.id ||
          $scope.definition.processDefinitionId ||
          $scope.definition.caseDefinitionId ||
          $scope.definition.decisionDefinitionId
        );
        return resource.updateHistoryTimeToLive(
          id,
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
