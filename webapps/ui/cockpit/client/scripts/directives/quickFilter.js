/**
  Defines a widget to filter activity instances in the activity tree

  @name quickFilter
  @memberof cam.cockpit.directives
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

'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/quick-filter.html', 'utf8');

var $ = require('jquery');

module.exports = function() {

    /**
      determine if an element has to be schown
     */
  function showElement(states, searched, $el) {
    if ($el.find('.selected').length) {
      return true;
    }

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

    template: template,

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

      function refresh() {
        setTimeout(scope.search, 200);
      }
      scope.$root.$on('instance-tree-selection-change', refresh);
      scope.$root.$on('instance-diagram-selection-change', refresh);

      var $holder = $(scope.holderSelector);

        // activate (or not) a input (name or state)
        // if the relevant attribute is present (might be empty)
      scope.showNameFilter = typeof attrs.nameFilter !== 'undefined';
      scope.showStateFilter = typeof attrs.stateFilter !== 'undefined';

      scope.search = function() {

        if (scope.quickFilters &&
              scope.quickFilters.running && scope.quickFilters.running.$viewValue &&
              scope.quickFilters.canceled && scope.quickFilters.canceled.$viewValue &&
              scope.quickFilters.completed && scope.quickFilters.completed.$viewValue) {

          scope.quickFilters.running.$setViewValue(false);
          scope.quickFilters.running.$render();
          scope.quickFilters.canceled.$setViewValue(false);
          scope.quickFilters.canceled.$render();
          scope.quickFilters.completed.$setViewValue(false);
          scope.quickFilters.completed.$render();
        }

        var searched = scope.showNameFilter && scope.quickFilters.name ? $.trim(scope.quickFilters.name.$viewValue) : '';

        var states = {
          running: !!scope.showStateFilter && !!scope.quickFilters.running && !!scope.quickFilters.running.$viewValue,
          canceled: !!scope.showStateFilter && !!scope.quickFilters.canceled && !!scope.quickFilters.canceled.$viewValue,
          completed: !!scope.showStateFilter && !!scope.quickFilters.completed && !!scope.quickFilters.completed.$viewValue
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
};
