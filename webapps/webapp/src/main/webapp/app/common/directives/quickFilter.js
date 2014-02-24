/* global ngDefine: false, require: false */
/**
  Defines a widget to filter activity instances in the activity tree

  @name quickFilter
  @memberof cam.common.directives
  @type angular.directive

  @author Valentin Vago <valentin.vago@camunda.com>

  @example
    <div cam-quick-filter
         state-filter
         name-filter
         holder-selector=".instance-tree"
         label-selector=".tree-node-group"
         item-selector=".tree-node-group">
    </div>
 */
ngDefine('camunda.common.directives', ['jquery'], function(module, $) {
  'use strict';

  module.directive('camQuickFilter', function() {

    /**
      determine if an element has to be schown
     */
    function showElement(states, searched, $el) {
      if (!!states.canceled ||
          !!states.running ||
          !!states.completed) {
        if (
          (!states.canceled  && $el.hasClass('state-canceled'))  ||
          (!states.running   && $el.hasClass('state-running'))   ||
          (!states.completed && $el.hasClass('state-completed'))
        ) {
          return false;
        }
      }

      if (!searched) {
        return true;
      }

      var exp = new RegExp(searched, 'i');
      return exp.test($.trim($el.text()));
    }

    return {
      scope: {
        holderSelector: '@',
        labelSelector: '@',
        itemSelector: '@'
      },

      restrict: 'A',

      templateUrl: require.toUrl('./app/common/directives/quick-filter.html'),

      link: function(scope, element, attrs) {
        if (!scope.holderSelector) {
          throw new Error('A holder-selector attribute must be specified');
        }
        if (!scope.labelSelector) {
          throw new Error('A label-selector attribute must be specified');
        }
        if (!scope.itemSelector) {
          throw new Error('A item-selector attribute must be specified');
        }

        var $holder = $(scope.holderSelector);

        // activate (or not) a input (name or state)
        // if the relevant attribute is present (might be empty)
        scope.showNameFilter = typeof attrs.nameFilter !== 'undefined';
        scope.showStateFilter = typeof attrs.stateFilter !== 'undefined';

        scope.search = function() {
          var searched = $.trim(scope.quickFilters.name.$viewValue);
          var states = {
            running: !!scope.quickFilters.running.$viewValue,
            canceled: !!scope.quickFilters.canceled.$viewValue,
            completed: !!scope.quickFilters.completed.$viewValue
          };

          $(scope.itemSelector, $holder).each(function() {
            var $el = $(this);
            var visible = showElement(states, searched, $el);
            $el[visible ? 'show' : 'hide']();
          });
        };

        scope.clearName = function() {
          scope.quickFilters.name.$setViewValue('');
          scope.quickFilters.name.$render();
          scope.search();
        };
      }
    };
  });
});
