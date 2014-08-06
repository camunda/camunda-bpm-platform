define([
  'angular',
  'moment',
  'text!./cam-tasklist-tasks.html'
], function(
  angular,
  moment,
  template
) {
  'use strict';
  return [
    '$modal',
    '$rootScope',
    '$timeout',
    '$q',
    'camTasklistPileFilterConversion',
    'camAPI',
  function(
    $modal,
    $rootScope,
    $timeout,
    $q,
    camTasklistPileFilterConversion,
    camAPI
  ) {
    var Task = camAPI.resource('task');
    return {
      link: function(scope) {
        var dateExp = /(Before|After)$/;

        scope.pageSize = 15;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.now = new Date();

        scope.loading = false;

        scope.tasks = scope.tasks || [];

        scope.pile = scope.pile || $rootScope.currentPile;

        scope.searchTask = '';

        scope.sorting = angular.element('[cam-sorting-choices]').scope();

        scope.sorting.$on('sorting.by.change', loadItems);

        scope.sorting.$on('sorting.order.change', loadItems);

        function setCurrentTask(task) {
          task._embedded = task._embedded || {};
          // if (!task._embedded.processDefinition) {
          //   task._embedded.processDefinition = taskProcessDefinition(task);
          // }
          $rootScope.currentTask = task;
          $rootScope.$broadcast('tasklist.task.current');
        }

        // function taskProcessDefinition(task) {
        //   if (task._embedded && task._embedded.processDefinition) {
        //     return task._embedded.processDefinition;
        //   }

        //   for (var p in scope.processDefinitions) {
        //     var procDef = scope.processDefinitions[p];
        //     if (procDef.id === task.processDefinitionId) {
        //       return procDef;
        //     }
        //   }
        // }

        function loadItems() {
          scope.loading = true;
          scope.tasks = [];

          var where = buildWhere(scope.sorting.order, scope.sorting.by);

          Task.list(where, function(err, res) {
            scope.loading = false;
            if (err) { throw err; }

            scope.totalItems = res.count;
            scope.processDefinitions = res._embedded.processDefinition;
            // TODO: refactor that when #CAM-2550 done
            scope.tasks = res._embedded.task || res._embedded.tasks;
            // angular.forEach(scope.tasks, function(task, t) {
            //   // taskProcessDefinition(task);
            // });
          });
        }

        function buildWhere(order, by) {
          var where = {};
          angular.forEach(scope.pile.filters, function(pair) {
            where[pair.key] = camTasklistPileFilterConversion(pair.value);
            if (dateExp.test(pair.key)) {
              /* jshint evil: true */
              var date = new Date(eval(where[pair.key]) * 1000);
              /* jshint evil: false */
              date = moment(date);
              where[pair.key] = date.toISOString();
            }
          });

          where.firstResult = (scope.pageNum - 1) * scope.pageSize;
          where.maxResults = scope.pageSize;

          if (order && by) {
            where.sortBy = by;
            where.sortOrder = order;
          }

          return where;
        }


        scope.pageChange = loadItems;


        scope.lookupTask = function(val) {
          var deferred = $q.defer();

          scope.loading = true;

          var where = buildWhere(scope.sorting.order, scope.sorting.by);

          where.nameLike = '%'+ val +'%';

          Task.list(where, function(err, res) {
            scope.loading = false;

            if (err) {
              return deferred.reject(err);
            }

            deferred.resolve(res._embedded.tasks);
          });

          return deferred.promise;
        };


        scope.selectedTask = function($item) {
          setCurrentTask($item);
          scope.searchTask = '';
        };


        scope.focus = function(delta) {
          setCurrentTask(scope.tasks[delta]);
        };


        scope.$on('tasklist.pile.current', function() {
          if (
            !$rootScope.currentPile ||
            (scope.pile && (scope.pile.id === $rootScope.currentPile.id))
          ) {
            return;
          }
          scope.pile = $rootScope.currentPile;
          loadItems();
        });
      },

      template: template
    };
  }];
});
