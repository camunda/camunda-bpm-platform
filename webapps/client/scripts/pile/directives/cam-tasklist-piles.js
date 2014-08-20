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
        '$rootScope',
        'camAPI',
      function (
        $scope,
        $rootScope,
        camAPI
      ) {
        var Pile = camAPI.resource('pile');

        $scope.piles = [];
        $scope.focusedId = null;
        $scope.loading = true;


        $scope.focus = function(pile) {
          if ($scope.focusedId === pile.id) { return; }
          $scope.focusedId = pile.id;
          $rootScope.currentPile = pile;
          $rootScope.$broadcast('tasklist.pile.current');
        };



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
            $scope.focus(res.items[0]);
          });
        }

        $rootScope.$watch('authentication', function() {
          listPiles();
        });
      }]
    };
  }];
});
