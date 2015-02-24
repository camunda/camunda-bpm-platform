define([
  'angular',
  'text!./cam-tasklist-sorting-choices.html'
], function(
  angular,
  template
) {
  'use strict';

  function stringifySortings(sortingQuery) {
    var items = sortingQuery.map(function (sorting) {
      return {
        sortBy: sorting.by,
        sortOrder: sorting.order
      };
    });
    return JSON.stringify(items);
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

      link: function(scope, element) {
        var $body = angular.element('body');

        function updateBodyClass(plus) {
          $body
            .removeClass('sort-choices-' + scope.sortings.length)
            .addClass('sort-choices-' + (scope.sortings.length + plus))
          ;
        }

        var tasklistData = scope.tasklistData.newChild(scope);

        scope.sortableProps = {
          priority:             $translate.instant('PRIORITY'),
          created:              $translate.instant('CREATION_DATE'),
          dueDate:              $translate.instant('DUE_DATE'),
          followUpDate:         $translate.instant('FOLLOW_UP_DATE'),
          nameCaseInsensitive:  $translate.instant('TASK_NAME'),
          assignee:             $translate.instant('ASSIGNEE')
        };
        var sorting = {
          order:    'desc',
          by:       null,
          byLabel:  null
        };
        scope.sortings = [
          angular.copy(sorting)
        ];
        scope.sortedOn = [];
        scope.sortLimit = Object.keys(scope.sortableProps).length;

        /**
         * observe the task list query
         */
        tasklistData.observe('taskListQuery', function(taskListQuery) {
          if (taskListQuery) {
            var urlSortings = JSON.parse(($location.search() || {}).sorting || '[]');

            scope.sortedOn = [];

            scope.sortings = urlSortings.map(function (sorting) {
              scope.sortedOn.push(sorting.sortBy);

              return {
                order:    sorting.sortOrder,
                by:       sorting.sortBy,
                byLabel:  scope.sortableProps[sorting.sortBy]
              };
            });

            updateBodyClass(0);
          }
        });

        /**
         * Invoked when adding a sorting object
         */
        scope.addSorting = function (by) {
          updateBodyClass(1);

          var newSorting = angular.copy(sorting);
          newSorting.by = by;
          scope.sortings.push(newSorting);

          scope.sortedOn = scope.sortings.map(function (sorting) {
            return sorting.by;
          });

          search.updateSilently({
            sorting: stringifySortings(scope.sortings)
          });

          tasklistData.changed('taskListQuery');
        };

        /**
         * Invoked when removing a sorting object
         */
        scope.removeSorting = function (index) {
          updateBodyClass(-1);

          var newSortings = [];
          scope.sortings.forEach(function (sorting, i) {
            if (i != index) {
              newSortings.push(sorting);
            }
          });
          scope.sortings = newSortings;

          scope.sortedOn = scope.sortings.map(function (sorting) {
            return sorting.by;
          });

          // update query
          search.updateSilently({
            sorting: stringifySortings(scope.sortings)
          });

          tasklistData.changed('taskListQuery');
        };

        /**
         * invoked when the sort order is changed
         */
        scope.changeOrder = function(index) {
          scope.sortings[index].order = scope.sortings[index].order === 'asc' ? 'desc' : 'asc';

          // update query
          search.updateSilently({
            sorting: stringifySortings(scope.sortings)
          });

          tasklistData.changed('taskListQuery');
        };

        /**
         * invoked when the sort property is changed
         */
        scope.changeBy = function(index, by) {
          // close dropdown
          element.find('.dropdown.open').removeClass('open');
          scope.sortings[index].by = by;

          scope.sortedOn = scope.sortings.map(function (sorting) {
            return sorting.by;
          });

          // // update query
          search.updateSilently({
            sorting: stringifySortings(scope.sortings)
          });

          tasklistData.changed('taskListQuery');
        };

      }
    };
  }];
});
