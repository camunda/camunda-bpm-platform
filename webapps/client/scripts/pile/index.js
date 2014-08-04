'use strict';



/**
 * @module  cam.tasklist.pile
 * @belongsto cam.tasklist
 *
 * Piles are predefined filters for tasks.
 */



define([
  'require',
  'angular',
  'moment',
  './directives/cam-tasklist-pile',
  './directives/cam-tasklist-piles',
  './directives/cam-tasklist-tasks',
  'camunda-tasklist-ui/utils',
  'camunda-tasklist-ui/api',
  'text!./form.html'
], function(
  require,
  angular,
  moment,
  camTasklistPile,
  camTasklistPiles,
  camTasklistPileTasks
) {

  var pileModule = angular.module('cam.tasklist.pile', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-tasklist-ui/api').name,
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);


  pileModule.factory('camTasklistPileFilterConversion', [
    '$rootScope',
  function(
    $rootScope
  ) {
    var tokenExp = /(\{[^\}]+\})/g;


    function tokenReplace(val) {
      var user = $rootScope.authentication;
      if (val === '{self}') {
        return user ? user.name : 'anonymous';
      }

      if (val === '{now}') {
        return Math.round((new Date()).getTime() / 1000);
      }

      if (val === '{day}') {
        return 60 * 60 * 24;
      }

      if (!tokenExp.test(val)) {
        return val;
      }

      var pieces = [];

      angular.forEach(val.split(tokenExp), function(piece) {
        pieces.push(tokenReplace(piece));
      });

      return pieces.join('');
    }

    return tokenReplace;
  }]);







  pileModule.controller('pilesCtrl', [
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
    $scope.loading = true;

    function listPiles() {
      Pile.list({}, function(err, res) {
        $scope.loading = false;
        if (err) {
          throw err;
        }

        $scope.piles = res.items;
        $rootScope.currentPile = $scope.piles[0];
        $rootScope.$broadcast('tasklist.pile.current');
      });
    }

    function authed() {
      return $rootScope.authentication && $rootScope.authentication.name;
    }

    if (authed()) {
      listPiles();
    }

    $rootScope.$watch('authentication', function() {
      if (authed()) {
        listPiles();
      }
    });
  }]);


  pileModule.controller('pileCreateModalCtrl', [
    '$modalInstance',
    '$scope',
  function(
    $modalInstance,
    $scope
  ) {
    $scope.createPile = function() {
      console.info('createPile', arguments);
    };

    $scope.addFilter = function() {
      console.info('addFilter', arguments, $scope);
    };

    $scope.abort = $modalInstance.dismiss;
  }]);


  pileModule.controller('pileCreateCtrl', [
    '$modal',
    '$scope',
  function(
    $modal,
    $scope
  ) {
    $scope.createPile = function() {
      $modal.open({
        // pass the current scope to the $modalInstance
        scope: $scope,

        size: 'lg',

        template: require('text!./form.html'),

        controller: 'pileCreateModalCtrl'
      });
    };
  }]);







  pileModule.directive('camTasklistPile', camTasklistPile);

  pileModule.directive('camTasklistPiles', camTasklistPiles);

  pileModule.directive('camTasklistPileTasks', camTasklistPileTasks);

  return pileModule;
});
