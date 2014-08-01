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
  'camunda-tasklist-ui/utils',
  'camunda-tasklist-ui/api',
  'text!camunda-tasklist-ui/pile/form.html',
  'text!camunda-tasklist-ui/pile/list.html',
  'text!camunda-tasklist-ui/pile/details.html',
  'text!camunda-tasklist-ui/pile/tasks.html'
], function(
  require,
  angular
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
  function($rootScope) {
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
           '$scope', '$rootScope', 'camAPI',
  function ($scope,   $rootScope,   camAPI) {
    var Pile = camAPI.resource('pile');

    $scope.piles = [];
    $scope.loading = true;

    Pile.list({}, function(err, res) {
      $scope.loading = false;
      if (err) {
        throw err;
      }

      $scope.piles = res.items;
      $rootScope.currentPile = $scope.piles[0];
      $rootScope.$broadcast('tasklist.pile.current');
    });
  }]);


  pileModule.controller('pileCreateModalCtrl', [
          '$modalInstance', '$scope',
  function($modalInstance,   $scope) {
    $scope.createPile = function() {
      console.info('createPile', arguments);
    };

    $scope.addFilter = function() {
      console.info('addFilter', arguments, $scope);
    };

    $scope.abort = $modalInstance.dismiss;
  }]);


  pileModule.controller('pileCreateCtrl', [
          '$modal', '$scope',
  function($modal,   $scope) {
    $scope.createPile = function() {
      // $rootScope.currentPile = blankPile;

      $modal.open({
        // pass the current scope to the $modalInstance
        scope: $scope,

        size: 'lg',

        template: require('text!camunda-tasklist-ui/pile/form.html'),

        controller: 'pileCreateModalCtrl'
      });
    };
  }]);







  pileModule.directive('camTasklistPile', [
          '$modal', '$rootScope',
  function($modal,   $rootScope) {
    return {
      link: function(scope, element) {
        scope.focus = function() {
          element
            .parent()
              .find('.task-pile')
                .removeClass('active')
          ;
          element
            .addClass('active')
            .find('.task-pile')
              .addClass('active')
          ;

          $rootScope.currentPile = scope.pile;
          $rootScope.$broadcast('tasklist.pile.current');
        };

        // scope.edit = function() {
        //   modalUID++;
        //   var pile = this.$parent.pile;

        //   var modalInstance = $modal.open({
        //     size: 'lg',

        //     controller: [
        //             '$scope',
        //     function($scope) {
        //       $scope.elUID = 'modal'+ modalUID;
        //       $scope.labelsWidth = 3;
        //       $scope.fieldsWidth = 9;
        //       $scope.pile = pile;

        //       $scope.addFilter = function() {
        //         console.info('add filter');
        //         $scope.pile.filters.push({
        //           key: '',
        //           operator: '',
        //           value: ''
        //         });
        //       };

        //       $scope.ok = function() {
        //         modalInstance.close($scope.pile);
        //       };

        //       $scope.abort = function() {
        //         modalInstance.dismiss('cancel');
        //       };
        //     }],
        //     templateUrl: 'scripts/pile/form.html'
        //   });

        //   modalInstance.result.then(function (pile) {
        //     console.info('completed', pile);
        //   }, function (reason) {
        //     console.info('rejected', reason);
        //   });
        // };

        if (scope.pile && scope.pile.color) {
          var style = {
            'background-color': scope.pile.color
          };
          element
            .css(style)
            .find('.task-pile')
              .css(style)
            .find('.info')
              .css(style)
          ;
        }
      },

      template: require('text!camunda-tasklist-ui/pile/details.html')
    };
  }]);




  pileModule.directive('camTasklistPiles', [
  function(
  ) {
    return {
      template: require('text!camunda-tasklist-ui/pile/list.html')
      // template: require('text!camunda-tasklist-ui/pile/directives/cam-tasklist-piles.html')
    };
  }]);




  pileModule.directive('camTasklistPileTasks', [
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

      template: require('text!camunda-tasklist-ui/pile/tasks.html')
    };
  }]);

  return pileModule;
});
