define([
  'text!./cam-tasklist-filters.html'
], function(
  template
) {
  'use strict';
  return [
  function(
  ) {


    function itemById(items, id) {
      var i, item;
      for (i in items) {
        item = items[i];
        if (item.id === id) { return item; }
      }
    }



    return {
      template: template,

      controller: [
        '$scope',
        '$rootScope',
        'camAPI',
      function (
        $scope,
        $rootScope,
        camAPI
      ) {
        var Filter = camAPI.resource('filter');

        var _scopeEvents = [];
        $scope.$on('$destroy', function() {
          if (!_scopeEvents.length) { return; }
          console.info('filters scope destroy');
          angular.forEach(_scopeEvents, function(fn) { fn(); });
        });

        $scope.filters = [];
        $scope.focusedId = null;
        $scope.loading = false;

        $scope.edit = function(filter) {
          $rootScope.$broadcast('tasklist.filter.edit', filter);
        };

        $scope.delete = function(filter) {
          $rootScope.$broadcast('tasklist.filter.delete', filter);
        };

        $scope.focus = function(filter) {
          if ($scope.focusedId === filter.id) { return; }
          $scope.focusedId = filter.id;
          $rootScope.currentFilter = filter;
          $rootScope.$broadcast('tasklist.filter.current');
        };



        function authed() {
          return $rootScope.authentication && $rootScope.authentication.name;
        }



        function listFilters() {
          if ($scope.loading || !authed()) { return; }
          $scope.loading = true;

          Filter.list({
            itemCount: true
          }, function(err, res) {
            $scope.loading = false;
            if (err) { throw err; }

            $scope.filters = res;

            var focused;
            angular.forEach(res, function(filter) {
              if ($scope.focusedId === filter.id) {
                focused = filter;
              }
              else if (!focused || filter.properties.priority < focused.properties.priority) {
                focused = filter;
              }
            });

            $scope.focus(focused);
          });
        }

        listFilters();

        _scopeEvents.push($rootScope.$on('tasklist.task.update', listFilters));

        _scopeEvents.push($rootScope.$on('tasklist.filter.saved', listFilters));

        _scopeEvents.push($rootScope.$on('tasklist.filter.deleted', listFilters));

        _scopeEvents.push($rootScope.$on('authentication.login.success', listFilters));
      }]
    };
  }];
});
