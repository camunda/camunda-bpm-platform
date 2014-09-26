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

  function itemById(items, id) {
    var index = indexOfId(items, id);
    if (index > -1) {
      return items[index];
    }
  }



  function fixReadDateTimezone(dateStr) {
    if (!dateStr) { return dateStr; }
    var d = new Date(dateStr);
    return (new Date(d.getTime() + (d.getTimezoneOffset() * 60 * 1000))).toJSON();
  }

  function fixWriteDateTimezone(dateObj) {
    return new Date(dateObj.getTime() - (dateObj.getTimezoneOffset() * 1000 * 60));
  }



  return [
    '$modal',
    '$location',
    '$rootScope',
    '$q',
    'camAPI',
  function(
    $modal,
    $location,
    $rootScope,
    $q,
    camAPI
  ) {
    var Task = camAPI.resource('task');
    var Filter = camAPI.resource('filter');

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

        scope.sorting = angular.element('[cam-sorting-choices]').scope();

        scope.sorting.$on('sorting.by.change', loadTasks);

        scope.sorting.$on('sorting.order.change', loadTasks);


        function authed() {
          return $rootScope.authentication &&
                  !!$rootScope.authentication.name;
        }


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
          if (!authed() || !scope.filter) { return; }

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



            angular.forEach(res._embedded.task, function(task, t) {
              // res._embedded.task[t]._serverDue = task.due;
              // res._embedded.task[t]._serverFollowUp = task.followUp;

              res._embedded.task[t].due = fixReadDateTimezone(task.due);
              res._embedded.task[t].followUp = fixReadDateTimezone(task.followUp);

              // console.info('fix dates', res._embedded.task[t]._serverDue, res._embedded.task[t].due);
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


        // scope.lookupTask = function(val) {
        //   var deferred = $q.defer();

        //   scope.loading = true;

        //   var where = buildWhere(scope.sorting.order, scope.sorting.by);

        //   where.nameLike = '%'+ val +'%';

        //   Task.list(where, function(err, res) {
        //     scope.loading = false;

        //     if (err) {
        //       return deferred.reject(err);
        //     }

        //     deferred.resolve(res._embedded.tasks);
        //   });

        //   return deferred.promise;
        // };


        // scope.selectedTask = function($item) {
        //   setCurrentTask($item);
        //   scope.searchTask = '';
        // };





        function saveDate(propName) {
          return function(inlineFieldScope) {
            var self = this;
            var task = angular.copy(self.task);

            task[propName] = fixWriteDateTimezone(inlineFieldScope.varValue);
            // task[propName] = inlineFieldScope.varValue;


            delete task._embedded;
            delete task._links;

            Task.update(task, function(err, result) {
              if (err) {
                throw err;
              }

              // scope.$emit('tasklist.task.'+ propName);

              loadTasks();
            });
          };
        }

        scope.saveFollowUpDate = saveDate('followUp');
        scope.saveDueDate = saveDate('due');




        scope.focus = function(delta) {
          setCurrentTask(scope.tasks[delta], true);
        };


        scope.$on('tasklist.process.start', loadTasks);


        scope.$on('tasklist.filter.current', function() {
          if ($rootScope.currentFilter) {
            scope.filter = $rootScope.currentFilter;
            loadTasks();
          }
        });


        scope.$on('tasklist.task.update', loadTasks);

        scope.$on('tasklist.task.complete', function() {
          setCurrentTask(null);
          // loadTasks();
        });


        scope.$on('filters.loaded', loadTasks);
      }
    };
  }];
});
