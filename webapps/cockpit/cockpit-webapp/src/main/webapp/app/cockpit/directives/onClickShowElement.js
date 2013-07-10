'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var hideButtonTemplate = '  <button type="button" ng-show="!hidden" ng-click="showElement()" class="arrow-button" style="position: absolute; float:left; top: 19px; left: 19px; z-index: 1">' + 
                           '    <i class="icon-chevron-right"></i>' +
                           '  </button>'; 
  
  var Directive = function () {
    return {
      restrict: 'EAC',
      template: hideButtonTemplate,
      replace: true,
      scope: {
        onClickShowElement: '@',
        selection: '=',
      },
      link: function(scope, element, attrs) {
        
        scope.hidden = true;
        
        scope.$watch('selection.elements.hidden', function (newValue) {
          if (!newValue) {
            return;
          }
          
          if (scope.onClickShowElement === newValue) {
            scope.hidden = false;
          }
        });
        
        scope.showElement = function () {
          scope.hidden = true;
          scope.selection.elements = {visible : scope.onClickShowElement };
          
          element.parent().show('fast');
        };
        
      }
    };
  };
  
  module
    .directive('onClickShowElement', Directive);
  
});
