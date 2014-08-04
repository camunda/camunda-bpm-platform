define([
  'angular',
  'text!./cam-tasklist-tasks.html'
], function(
  angular,
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
        scope.pageSize = 15;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.now = new Date();

        scope.loading = false;

        scope.tasks = scope.tasks || [];

        scope.pile = scope.pile || $rootScope.currentPile;

        scope.searchTask = '';

        scope.sorting = angular.element('[cam-sorting-choices]').scope();

        scope.sorting.$on('sorting.by.change', function(/*info, value*/) {
          // console.info('sorting.by.change', value);
          loadItems();
        });
        scope.sorting.$on('sorting.order.change', function(/*info, value*/) {
          // console.info('sorting.order.change', value);
          loadItems();
        });

        scope.lookupTask = function(val) {
          var deferred = $q.defer();

          scope.loading = true;

          var where = {};
          angular.forEach(scope.pile.filters, function(pair) {
            where[pair.key] = camTasklistPileFilterConversion(pair.value);
          });
          where.firstResult = (scope.pageNum - 1) * scope.pageSize;
          where.maxResults = scope.pageSize;
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
          console.info('scope', scope);
          $rootScope.currentTask = $item;
          $rootScope.$broadcast('tasklist.task.current');
          scope.searchTask = '';
        };


        function loadItems() {
          scope.loading = true;
          scope.tasks = [];

          var where = {};
          angular.forEach(scope.pile.filters, function(pair) {
            where[pair.key] = camTasklistPileFilterConversion(pair.value);
          });
          where.firstResult = (scope.pageNum - 1) * scope.pageSize;
          where.maxResults = scope.pageSize;

          where.sortBy = scope.sorting.by;
          where.sortOrder = scope.sorting.order;

          Task.list(where, function(err, res) {
            scope.loading = false;

            if (err) {
              throw err;
            }

            scope.totalItems = res.count;
            scope.tasks = res._embedded.tasks;
          });
        }


        scope.pageChange = loadItems;


        scope.focus = function(delta) {
          $rootScope.currentTask = scope.tasks[delta];
          $rootScope.$broadcast('tasklist.task.current');
        };


        // scope.batchOperationSelect = function() {
        //   console.info('selected task', this);
        // };

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
