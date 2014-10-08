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

  function indexOfId(items, id) {
    for (var i in items) {
      if (items[i].id === id) { return i; }
    }
    return -1;
  }

  return [
    '$location',
    '$rootScope',
    'camAPI',
  function(
    $location,
    $rootScope,
    camAPI
  ) {
    var Filter = camAPI.resource('filter');

    return {
      scope: {
        filter: '='
      },

      template: template,

      link: function(scope, element) {
        var _scopeEvents = [];
        element.on('$destroy', function() {
          if (!_scopeEvents.length) { return; }
          angular.forEach(_scopeEvents, function(fn) { fn(); });
        });

        scope.pageSize = 15;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.loading = false;

        scope.tasks = scope.tasks || [];

        scope.now = (new Date()).toJSON();

        scope.sorting = angular.element('[cam-sorting-choices]').scope();

        _scopeEvents.push(scope.sorting.$on('sorting.by.change', loadTasks));

        _scopeEvents.push(scope.sorting.$on('sorting.order.change', loadTasks));


        function authed() {
          return $rootScope.authentication &&
                  !!$rootScope.authentication.name;
        }


        function setCurrentTask(task, silent) {
          if (!task && !$rootScope.currentTask) {
            return;
          }

          if (task && $rootScope.currentTask && task.id === $rootScope.currentTask.id) {
            return;
          }

          $rootScope.currentTask = task;
          $rootScope.$broadcast('tasklist.task.current', task);

          if (silent) {
            return;
          }

          // if there's a task, we pass its ID to the URL,
          // otherwise we clear the URL
          $location.search(task ? {
            task: task.id
          } : {});
        }


        function loadTasks() {
          if (scope.loading || !authed() || !scope.filter) { return; }

          scope.loading = true;
          scope.tasks = [];

          var where = buildWhere(scope.sorting.order, scope.sorting.by);

          Filter.getTasks(where, function(err, res) {
            scope.loading = false;
            if (err) { throw err; }

            scope.totalItems = res.count;
            scope.processDefinitions = scope.processDefinitions || [];


            angular.forEach(res._embedded ? res._embedded.processDefinition : [], function(procDef) {
              if (indexOfId(scope.processDefinitions) === -1) {
                scope.processDefinitions.push(procDef);
              }
            });

            scope.tasks = res._embedded.task;
          });
        }


        function buildWhere(order, by) {
          var where = {
            id: scope.filter.id
          };

          where.firstResult = (scope.pageNum - 1) * scope.pageSize;
          where.maxResults = scope.pageSize;

          if (order && by) {
            where.sortBy = by;
            where.sortOrder = order;
          }

          return where;
        }


        scope.pageChange = loadTasks;


        scope.focus = function(delta) {
          setCurrentTask(scope.tasks[delta], true);
        };


        scope.$on('tasklist.process.start', loadTasks);


        scope.$on('tasklist.filter.current', function() {
          if (scope.filter && $rootScope.currentFilter) {
            if (scope.filter.id !== $rootScope.currentFilter.id) {
              scope.filter = $rootScope.currentFilter;
              loadTasks();
            }
          }
          else if ($rootScope.currentFilter) {
            scope.filter = $rootScope.currentFilter;
            loadTasks();
          }
        });


        loadTasks();

        _scopeEvents.push(scope.$on('tasklist.task.update', loadTasks));

        _scopeEvents.push(scope.$on('tasklist.task.complete', function() {
          setCurrentTask(null);
        }));

        _scopeEvents.push($rootScope.$on('tasklist.filter.deleted', loadTasks));

        _scopeEvents.push($rootScope.$on('tasklist.filter.saved', loadTasks));

        _scopeEvents.push($rootScope.$on('authentication.login.success', loadTasks));
      }
    };
  }];
});
