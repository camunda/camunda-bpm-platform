'use strict';

module.exports = ['$parse', '$rootScope', function($parse, $rootScope) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      var callbackFunc;

      attrs.$observe('isSidebarCollapsed', function(attribute) {
        callbackFunc = $parse(attribute);
        notififyCallback();
      });

      var removeRestoreListner = $rootScope.$on('restore', notififyCallback);
      var removeMaximizeListener = $rootScope.$on('maximize', notififyCallback);
      var removeResizeListener = $rootScope.$on('resize', notififyCallback);

      scope.$on('$destroy', function() {
        removeRestoreListner();
        removeMaximizeListener();
        removeResizeListener();
      });     

      function notififyCallback() {
        var collapsed = isCollapsed();

        if (callbackFunc && typeof callbackFunc === 'function') {
          callbackFunc(scope, {
            collapsed: collapsed
          });
        }
      }

      function isCollapsed() {
        return element.hasClass('collapsed');
      }
    }
  };
}];
