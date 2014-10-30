define([
  'angular',
  'text!./cam-layout-switcher.html'
], function(
  angular,
  template
) {
  'use strict';

  var $bdy = angular.element('body');

  var layouts = [
    {
      label: 'standard'
    },
    {
      label: 'list',
      css: 'layout-focus-list'
    },
    {
      label: 'task',
      css: 'layout-focus-task'
    }
  ];

  var layoutClasses = layouts.map(function(layout) {
    return layout.css || '';
  }).join(' ');

  return [
    '$location',
    'search',
  function(
    $location,
    search
  ) {
    return {
      restrict: 'EAC',

      link: function(scope) {
        function applyLayout(delta) {
          var layout = layouts[delta];
          scope.activeLayout = delta;
          scope.activeLayoutInfo = layout;

          $bdy
            .removeClass(layoutClasses)
            .addClass(layout.css)
          ;
        }

        function applyRouteLayout() {
          var state = $location.search();
          var delta = parseInt(state.layout ? state.layout : 0, 10);
          applyLayout(delta);
        }

        applyRouteLayout();
        scope.$on('$routeChanged', applyRouteLayout);

        scope.activeLayoutInfo = layouts[scope.activeLayout];

        scope.layouts = layouts;


        scope.switchLayout = function(delta) {
          var state = $location.search();
          state.layout = delta;
          search.updateSilently(state);
          applyLayout(delta);
        };
      },

      template: template
    };
  }];
});
