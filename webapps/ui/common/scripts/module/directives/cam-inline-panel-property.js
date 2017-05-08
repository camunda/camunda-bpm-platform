'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-inline-panel-property.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      property: '=camInlinePanelProperty',
      format: '&',
      onChange: '&',
      onRemove: '&',
      type: '@'
    },
    link: function($scope) {
      $scope.remove = function($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.onRemove();
      };
    }
  };
};
