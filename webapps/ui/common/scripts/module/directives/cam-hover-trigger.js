'use strict';

module.exports = function() {
  return {
    restrict: 'A',
    require: '^camHoverArea',
    link: function($scope, $element, $attr, HoverArea) {
      $element.on('mouseenter', function() {
        $scope.$apply(function() {
          HoverArea.hoverTitle($attr.camHoverTrigger);
        });
      });

      $element.on('mouseleave', function() {
        $scope.$apply(HoverArea.cleanHover.bind(HoverArea));
      });
    }
  };
};
