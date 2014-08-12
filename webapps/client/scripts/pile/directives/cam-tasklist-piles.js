define([
  'text!./cam-tasklist-piles.html'
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
        '$location',
        '$rootScope',
        'camAPI',
      function (
        $scope,
        $location,
        $rootScope,
        camAPI
      ) {
        var Pile = camAPI.resource('pile');

        $scope.piles = [];
        $scope.focusedId = null;
        $scope.loading = true;



        function focus() {
          var state = $location.search();
          var pile = state.tasks ? itemById($scope.piles, state.tasks) : $scope.piles[0];

          $scope.focusedId = pile ? pile.id : null;
          $rootScope.currentPile = pile;
          $rootScope.$broadcast('tasklist.pile.current');
        }



        function authed() {
          return $rootScope.authentication && $rootScope.authentication.name;
        }



        function listPiles() {
          if (!authed()) { return; }

          Pile.list({}, function(err, res) {
            $scope.loading = false;
            if (err) {
              throw err;
            }

            $scope.piles = res.items;
            if ($scope.piles.length) {
              focus();
            }
          });
        }



        listPiles();



        $rootScope.$on('$locationChangeSuccess', function() {
          focus();
        });

        $rootScope.$watch('authentication', function() {
          listPiles();
        });
      }]
    };
  }];
});
