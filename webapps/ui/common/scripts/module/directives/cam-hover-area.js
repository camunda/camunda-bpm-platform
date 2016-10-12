'use strict';

module.exports = function() {
  return {
    restrict: 'A',
    transclude: true,
    scope: true,
    controller: 'HoverAreaController as HoverArea',
    link: function($scope, $element, $attrs, $ctrl, $transclude) {
      $transclude(function(content) {
        $element.empty();
        $element.append(content);
      });
    }
  };
};

