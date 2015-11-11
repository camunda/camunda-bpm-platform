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
    '$timeout',
  function(
    search,
    $translate,
    $location,
    $timeout
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

        scope.sortings = [{
          order:    'desc',
          by:       'created'
        }];

        scope.openDropdowns = [];
        scope.openDropdownNew = false;

        scope.sortedOn = [];

        var plannedRefresh;
        function updateColumns() {
          if (plannedRefresh) {
            $timeout.cancel(plannedRefresh);
          }

          plannedRefresh = $timeout(function () {
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
            plannedRefresh = null;
          }, 100);
        }

        scope.$on('layout:change', updateColumns);

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
            return scope.uniqueProps[by];
          }

          if (!scope.sortings[index] || !scope.sortings[index].parameters) {
            return '';
          }

          return scope.sortings[index].parameters.variable;
        };

        /**
         * observe the task list query
         */
        var tasklistData = scope.tasklistData.newChild(scope);

        tasklistData.observe('taskListQuery', function(taskListQuery) {
          if (taskListQuery) {
            var urlSortings = JSON.parse(($location.search() || {}).sorting || '[]');

            scope.sortedOn = [];
            scope.openDropdowns = [];

            scope.availableOptions = angular.copy(scope.uniqueProps);

            scope.sortings = urlSortings.map(function (sorting) {
              scope.sortedOn.push(sorting.sortBy);
              scope.openDropdowns.push(false);

              delete scope.availableOptions[sorting.sortBy];

              var returned = {
                order:      sorting.sortOrder,
                by:         sorting.sortBy
              };

              if (sorting.parameters) {
                returned.parameters = sorting.parameters;
              }

              return returned;
            });


            if (!scope.sortings.length) {
              scope.addSorting('created');
            }

            updateColumns();
          }
        });


        scope.$watch('sortings.length', function (now, before) {
          if (now !== before) { scope.updateSortings(); }
        });

        scope.$watch('sortings', updateColumns, true);

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

        scope.changeSorting = function(idx, id, type, value) {
          scope.sortings[idx].by = id;
          delete scope.sortings[idx].parameters;
          if(type) {
            scope.sortings[idx].parameters = {
              variable : value,
              type     : type
            };
          }

          scope.updateSortings();
        };

        scope.resetFunctions = [];
        scope.openDropdown = function(idx, open) {
          if(open) {
            var sorting = scope.sortings[idx];
            if(sorting) {
              scope.resetFunctions[idx](sorting.by, sorting.parameters && sorting.parameters.type, sorting.parameters && sorting.parameters.variable);
            } else {
              scope.resetFunctions[idx]();
            }

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

          updateColumns();
        };

        /**
         * Invoked when adding a sorting object
         */
        scope.addSorting = function (id, type, value) {

          var newSorting = {
            order: 'desc',
            by: id
          };
          if(type) {
            newSorting.parameters = {
              variable : value,
              type     : type
            };
          }
          scope.sortings.push(newSorting);

          scope.updateSortings();
        };

        /**
         * Invoked when removing a sorting object
         */
        scope.removeSorting = function (index) {
          scope.sortings.splice(index, 1);
          scope.updateSortings();
        };

        /**
         * invoked when the sort order is changed
         */
        scope.changeOrder = function(index) {
          scope.sortings[index].order = scope.sortings[index].order === 'asc' ? 'desc' : 'asc';

          scope.updateSortings();
        };
      }
    };
  }];
});
