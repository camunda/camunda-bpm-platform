'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var hideButtonTemplate = '  <button type="button" ng-show="!hidden" ng-click="hideElement()" class="arrow-button">' + 
                           '    <i class="icon-chevron-left"></i>' +
                           '  </button>'; 
  
  var Directive = function () {
    return {
      restrict: 'EAC',
      template: hideButtonTemplate,
      replace: true,
      scope: {
        onClickHideElement: '@',
        selection: '=',
      },
      link: function(scope, element, attrs) {

        scope.$watch('selection.elements.visible', function (newValue) {
          if (!newValue) {
            return;
          }
          if (newValue === scope.onClickHideElement) {
            element.parent().show('fast');  
          }
        });
        
        scope.hideElement = function () {
          element.parent().hide('fast');

          setTimeout(function () {
            scope.selection.elements = {hidden: scope.onClickHideElement };
            scope.$apply();
          }, 750);
        };
        
      }
    };
  };
  
  module
    .directive('onClickHideElement', Directive);
  
});
