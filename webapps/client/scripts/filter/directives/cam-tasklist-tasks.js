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

  function itemById(items, id) {
    var i, item;
    for (i in items) {
      item = items[i];
      if (item.id === id) { return item; }
    }
  }

  return [
    '$modal',
    '$location',
    '$rootScope',
    '$q',
    'camTasklistFilterFilterConversion',
    'camAPI',
  function(
    $modal,
    $location,
    $rootScope,
    $q,
    camTasklistFilterFilterConversion,
    camAPI
  ) {
    var Task = camAPI.resource('task');

    return {
      scope: {},

      template: template,

      link: function(scope) {
        var dateExp = /(Before|After)$/;

        scope.pageSize = 15;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.now = new Date();

        scope.loading = false;

        scope.tasks = scope.tasks || [];

        scope.filter = scope.filter || $rootScope.currentFilter;

        scope.searchTask = '';


        scope.sorting = angular.element('[cam-sorting-choices]').scope();

        scope.sorting.$on('sorting.by.change', loadTasks);

        scope.sorting.$on('sorting.order.change', loadTasks);


        function setCurrentTask(task, silent) {
          $rootScope.currentTask = task;
          if (!silent) {
            // if there's a task, we pass its ID to the URL,
            // otherwise we clear the URL
            $location.search(task ? {
              task: task.id
            } : {});

            $rootScope.$broadcast('tasklist.task.current');
          }
        }


        function loadTasks() {
          scope.loading = true;
          // scope.tasks = [];

          if (arguments[0]) {
            console.info('loadTasks because of', arguments[0].name);
          }

          var where = buildWhere(scope.sorting.order, scope.sorting.by);

          Task.list(where, function(err, res) {
            scope.loading = false;
            if (err) { throw err; }

            scope.totalItems = res.count;
            scope.processDefinitions = res._embedded.processDefinition;
            // TODO: refactor that when #CAM-2550 done
            scope.tasks = res._embedded.task || res._embedded.tasks;
          });
        }


        function buildWhere(order, by) {
          var where = {};
          angular.forEach(scope.filter.filters, function(pair) {
            where[pair.key] = camTasklistFilterFilterConversion(pair.value);
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


        scope.pageChange = loadTasks;


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
          setCurrentTask(scope.tasks[delta], true);
        };


        scope.$on('tasklist.filter.current', function() {
          if ($rootScope.currentFilter) {
            scope.filter = $rootScope.currentFilter;
            loadTasks();
          }
        });


        scope.$on('tasklist.task.assign', loadTasks);

        scope.$on('tasklist.task.delegate', loadTasks);

        scope.$on('tasklist.task.claim', loadTasks);

        scope.$on('tasklist.task.unclaim', loadTasks);

        scope.$on('tasklist.task.complete', function() {
          setCurrentTask(null);
          loadTasks();
        });
      }
    };
  }];
});
