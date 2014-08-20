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

        $scope.filters = [];
        $scope.focusedId = null;
        $scope.loading = true;


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
          if (!authed()) { return; }

          Filter.list({}, function(err, res) {
            $scope.loading = false;
            if (err) {
              throw err;
            }

            $scope.filters = res.items;
            $scope.focus(res.items[0]);
          });
        }

        $rootScope.$watch('authentication', function() {
          listFilters();
        });
      }]
    };
  }];
});
