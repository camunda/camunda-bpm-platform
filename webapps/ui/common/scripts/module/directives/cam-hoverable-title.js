'use strict';

module.exports = function() {
  return {
    restrict: 'A',
    require: '^camHoverArea',
    link: function($scope, $element, $attr, HoverArea) {
      var removeHoverListener;
      var hoverClass = $attr.hoverClass || 'hovered';

      $attr.$observe('camHoverableTitle', function(title) {
        cleanUp();

        removeHoverListener = HoverArea.addHoverListener(title, function(isHovered) {
          if (isHovered) {
            $element.addClass(hoverClass);
          } else {
            $element.removeClass(hoverClass);
          }
        });
      });

      $element.on('$destroy', cleanUp);

      function cleanUp() {
        if (removeHoverListener) {
          removeHoverListener();
        }
      }
    }
  };
};
