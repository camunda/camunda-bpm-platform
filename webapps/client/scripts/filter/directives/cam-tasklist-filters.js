define([
  'text!./cam-tasklist-filters.html',
  'angular'
], function(
  template,
  angular
) {
  'use strict';
  var $ = angular.element;
  var each = angular.forEach;

  function itemById(items, id) {
    var i, item;
    for (i in items) {
      item = items[i];
      if (item.id === id) { return item; }
    }
  }

  return [function() {

    return {
      template: template,

      controller: [
        '$scope',
        '$rootScope',
        '$timeout',
        'camAPI',
      function (
        $scope,
        $rootScope,
        $timeout,
        camAPI
      ) {
        var Filter = camAPI.resource('filter');

        var _scopeEvents = [];
        $scope.$on('$destroy', function() {
          if (!_scopeEvents.length) { return; }
          each(_scopeEvents, function(fn) { fn(); });
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
          if (filter) {
            if ($scope.focusedId === filter.id) { return; }
            $scope.focusedId = filter.id;
            $rootScope.currentFilter = filter;
          }
          else {
            $scope.focusedId = null;
            $rootScope.currentFilter = null;
          }
          $rootScope.$broadcast('tasklist.filter.current', filter);
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
            each(res, function(filter) {
              if ($scope.focusedId) {
                if ($scope.focusedId === filter.id) {
                  focused = filter;
                }
              }
              else if (!focused || filter.properties.priority < focused.properties.priority) {
                focused = filter;
              }
            });

            $scope.focus(focused);

            // this is aimed to fix the color of filters, because angular don't
            // want to "render" what is in a "conventionnal" HTML arguments
            // means.. you can not use something like:
            // style="background-color: {{color}}"
            // in angular templates
            $timeout(function() {
              each($scope.filters, function(filter) {
                if (filter.properties.color) {
                  $('[data-filter-id="'+ filter.id +'"]')
                    .find('.task-filter, .task-filter .info')
                    .css({
                      'background-color': filter.properties.color
                    })
                  ;
                }
              });
            }, 10);
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
