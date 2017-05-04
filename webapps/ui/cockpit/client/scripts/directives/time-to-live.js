'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/time-to-live.html', 'utf8');

module.exports = ['camAPI', '$window', function(camAPI, $window) {
  var resource = camAPI.resource('process-definition');

  return {
    restrict: 'A',
    template: template,
    scope: {
      processDefinition: '=timeToLive'
    },
    link: function($scope) {
      $scope.onChange = function() {
        $window.setTimeout(function() {
          resource.updateHistoryTimeToLive(
            $scope.processDefinition.id,
            {
              historyTimeToLive: +$scope.processDefinition.historyTimeToLive
            }
          );
        });
      };

      $scope.format = function(property) {
        if (property === 1) {
          return property + ' day';
        }

        return property + ' days';
      };
    }
  };
}];
