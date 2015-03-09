define([
  'angular',
  'text!./cam-tasklist-sorting-choices.html'
], function(
  angular,
  template
) {
  'use strict';

  function stringifySortings(sortingQuery) {
    return JSON.stringify(sortingQuery.map(function (sorting) {
      var obj = {
        sortBy: sorting.by,
        sortOrder: sorting.order
      };

      if (sorting.by.indexOf('Variable') > -1) {
        if (!sorting.parameters) {
          throw new Error('Variable sorting needs parameters');
        }
        obj.parameters = sorting.parameters;
      }

      return obj;
    }));
  }

  return [
    'search',
    '$translate',
    '$location',
  function(
    search,
    $translate,
    $location
  ) {
    return {

      restrict: 'A',

      scope: {
        tasklistData: '='
      },

      template: template,

      controller: [function () {}],

      link: function(scope, element) {
        var $bdy = angular.element('body');
        var $newSort = element.find('.new-sort .dropdown-menu');

        var sorting = {
          order:    'desc',
          by:       'created'
        };

        scope.sortings = [angular.copy(sorting)];

        scope.openDropdowns = [];
        scope.openDropdownNew = false;

        scope.sortedOn = [];

        function updateBodyClass() {
          var columns = element.parents('.columns');
          var headers = columns.find('.cell.top');
          var bodies = columns.find('.cell.content');
          var shown = $bdy.hasClass('list-column-close');

          element.css('height', 'auto');

          if (shown) {
            var minHeight = parseInt(headers.css('min-height'), 10);
            headers.css('height', minHeight);
            bodies.css('top', minHeight);
            return;
          }

          var height = element.height();
          var columnTop = element.parent();
          columnTop.height(height);
          var columnTopHeight = height;

          headers.height(columnTopHeight);
          bodies.css('top', columnTopHeight + 30);
        }

        scope.$on('layout:change', updateBodyClass);

        scope.uniqueProps = {
          priority:               $translate.instant('PRIORITY'),
          created:                $translate.instant('CREATION_DATE'),
          dueDate:                $translate.instant('DUE_DATE'),
          followUpDate:           $translate.instant('FOLLOW_UP_DATE'),
          nameCaseInsensitive:    $translate.instant('TASK_NAME'),
          assignee:               $translate.instant('ASSIGNEE')
        };

        scope.byLabel = function (index) {
          if (!scope.sortings[index]) {
            return '';
          }

          var by = scope.sortings[index].by;

          if (scope.uniqueProps[by]) {
            return scope.uniqueProps[by].toLowerCase();
          }

          if (!scope.sortings[index] || !scope.sortings[index].parameters) {
            return '';
          }

          return scope.sortings[index].parameters.variable;
        };

        scope.sortLimit = Object.keys(scope.uniqueProps).length;

        /**
         * observe the task list query
         */
        var tasklistData = scope.tasklistData.newChild(scope);

        tasklistData.observe('taskListQuery', function(taskListQuery) {
          if (taskListQuery) {
            var urlSortings = JSON.parse(($location.search() || {}).sorting || '[]');

            scope.sortedOn = [];
            scope.openDropdowns = [];

            scope.sortings = urlSortings.map(function (sorting) {
              scope.sortedOn.push(sorting.sortBy);
              scope.openDropdowns.push(false);

              var returned = {
                order:      sorting.sortOrder,
                by:         sorting.sortBy
              };

              if (sorting.parameters) {
                returned.parameters = sorting.parameters;
              }

              return returned;
            });

            updateBodyClass();

            if (!scope.sortings.length) {
              scope.addSorting('created');
            }
          }
        });


        scope.$watch('sortings.length', function (now, before) {
          if (now !== before) { scope.updateSortings(); }
        });

        function positionDropdown(el) {
          var edgeLeft = el.parent().position().left;
          var edgeRight = el.outerWidth() + edgeLeft;
          if (edgeRight > element.outerWidth()) {
            el.css('left', (element.outerWidth() - edgeRight) + 'px');
          }
        }

        scope.$watch('openDropdowns', function (now) {
          var index = now.indexOf(true);
          var els = element
                      .find('li.sorting-choice .dropdown-menu')
                      .css('left', 'auto');
          if (index > -1 && els[index]) {
            positionDropdown(angular.element(els[index]));
          }
        }, true);

        scope.$watch('openDropdownNew', function (now) {
          if (now) {
            positionDropdown($newSort);
          }
          else {
            $newSort.css('left', 'auto');
          }
        });

        scope.newSortingToggle = function (open) {
          if (open) {
            var newSortingScope = element.find('[sorting="newSorting"]').scope();
            newSortingScope.$parent.focusedOn = null;
            newSortingScope.sorting = {};
          }
        };


        // should NOT manipulate the `scope.sortings`!
        scope.updateSortings = function () {
          scope.openDropdowns = [];
          scope.sortedOn = scope.sortings.map(function (sorting) {
            scope.openDropdowns.push(false);
            return sorting.by;
          });

          search.updateSilently({
            sorting: stringifySortings(scope.sortings)
          });

          tasklistData.changed('taskListQuery');
        };

        /**
         * Invoked when adding a sorting object
         */
        scope.addSorting = function (by, order) {
          updateBodyClass();
          order = order || 'desc';

          var newSorting = angular.copy(sorting);
          newSorting.by = by;
          scope.sortings.push(newSorting);

          scope.updateSortings();
        };

        /**
         * Invoked when removing a sorting object
         */
        scope.removeSorting = function (index) {
          updateBodyClass();

          var newSortings = [];
          scope.sortings.forEach(function (sorting, i) {
            if (i != index) {
              newSortings.push(sorting);
            }
          });
          scope.sortings = newSortings;

          scope.updateSortings();
        };

        /**
         * invoked when the sort order is changed
         */
        scope.changeOrder = function(index) {
          scope.sortings[index].order = scope.sortings[index].order === 'asc' ? 'desc' : 'asc';

          scope.updateSortings();
        };

        /**
         * invoked when the sort property is changed
         */
        scope.changeBy = function(index, by) {
          scope.sortings[index].by = by;

          scope.updateSortings();
        };
      }
    };
  }];
});
