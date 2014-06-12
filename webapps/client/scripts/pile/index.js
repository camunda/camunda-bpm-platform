'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */

/**
 * @module  cam.tasklist.pile
 * @belongsto cam.tasklist
 *
 * Piles are predefined filters for tasks.
 */



define([
           'require', 'angular', 'moment',
           'camunda-tasklist-ui/utils',
           'camunda-tasklist-ui/pile/data',
           'camunda-tasklist-ui/task/data',
           'text!camunda-tasklist-ui/pile/form.html',
           'text!camunda-tasklist-ui/pile/list.html',
           'text!camunda-tasklist-ui/pile/details.html',
           'text!camunda-tasklist-ui/pile/tasks.html'
], function(require,   angular,   moment) {
  var pileModule = angular.module('cam.tasklist.pile', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-tasklist-ui/pile/data').name,
    require('camunda-tasklist-ui/task/data').name,
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  var modalUID = 0;
  var $ = angular.element;


  pileModule.factory('camTasklistPileFilterConversion', [
          'camStorage',
  function(camStorage) {
    var tokenExp = /(\{[^\}]+\})/g;
    function tokenReplace(val) {
      if (val === '{self}') {
        return camStorage.get('user').id;
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
          $rootScope.$emit('tasklist.pile.current');
        };

        scope.edit = function() {
          modalUID++;
          var pile = this.$parent.pile;

          var modalInstance = $modal.open({
            size: 'lg',

            controller: [
                    '$scope',
            function($scope) {
              $scope.elUID = 'modal'+ modalUID;
              $scope.labelsWidth = 3;
              $scope.fieldsWidth = 9;
              $scope.pile = pile;

              $scope.addFilter = function() {
                console.info('add filter');
                $scope.pile.filters.push({
                  key: '',
                  operator: '',
                  value: ''
                });
              };

              $scope.ok = function() {
                modalInstance.close($scope.pile);
              };

              $scope.abort = function() {
                modalInstance.dismiss('cancel');
              };
            }],
            templateUrl: 'scripts/pile/form.html'
          });

          modalInstance.result.then(function (pile) {
            console.info('completed', pile);
          }, function (reason) {
            console.info('rejected', reason);
          });
        };

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
          '$modal', '$rootScope',
  function($modal,   $rootScope) {
    return {
      template: require('text!camunda-tasklist-ui/pile/list.html')
    };
  }]);




  pileModule.directive('camTasklistPileTasks', [
          '$modal', '$rootScope', 'camTasklistPileFilterConversion', 'camTaskData',
  function($modal,   $rootScope,   camTasklistPileFilterConversion,   camTaskData) {
    return {
      link: function(scope) {
        scope.pageSize = 5;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.now = new Date();

        scope.tasks = scope.tasks || [];

        scope.pile = scope.pile || $rootScope.currentPile;

        function loadItems() {
          var where = {};
          angular.forEach(scope.pile.filters, function(pair) {
            where[pair.key] = camTasklistPileFilterConversion(pair.value);
          });
          where.offset = (scope.pageNum - 1) * scope.pageSize;
          where.limit = scope.pageSize;

          camTaskData
            .query(where)
            // QUESTION: results? or only tasks? which level should be abstracted/available?
            // What about a `result.total` property?
            // What will I do eat for lunch?
            .then(function(results) {
              scope.totalItems = results.total;
              results._embedded = results._embedded || {};
              results._embedded.tasks = results._embedded.tasks || [];
              $rootScope.currentPile.tasks = scope.tasks = results._embedded.tasks;
            }, function(err) {
              console.warn('tasklist.pile.current tasks', err);
            });
        }


        scope.pageChange = loadItems;


        scope.focus = function(delta) {
          $rootScope.currentTask = scope.tasks[delta];
          $rootScope.$emit('tasklist.task.current');
        };


        scope.batchOperationSelect = function() {
          console.info('selected task', this);
        };


        $rootScope.$watch('currentPile', function() {
          console.info('currentPile thingy', $rootScope.currentPile, scope.pile);
          if (!$rootScope.currentPile || (scope.pile && (scope.pile.id === $rootScope.currentPile.id))) {
            return;
          }

          scope.pile = $rootScope.currentPile;
          loadItems();
        });
      },

      template: require('text!camunda-tasklist-ui/pile/tasks.html')
    };
  }]);







  pileModule.controller('pilesCtrl', [
           '$scope', '$rootScope', '$modal', 'camPileData',
  function ($scope,   $rootScope,   $modal,   camPileData) {
    $scope.piles = [];

    camPileData.query({
      user: $scope.user
    }).then(function(piles) {
      console.info('camPileData piles', piles);
      $scope.piles = piles;
      $rootScope.currentPile = $scope.piles[2];
      $rootScope.$emit('tasklist.pile.current');
    }, function(err) {
      console.info('camPileData query error', err.stack);
    });
  }]);


  pileModule.controller('pileNewCtrl', [
          '$modal', '$scope', '$rootScope',
  function($modal,   $scope,   $rootScope) {
    $rootScope.currentPile = {
      name: '',
      description: '',
      color: '',
      filters: []
    };

    $('.task-board').addClass('pile-edit');

    var modalInstance = $modal.open({
      // pass the current scope to the $modalInstance
      scope: $scope,

      size: 'lg',

      template: require('text!camunda-tasklist-ui/pile/form.html'),

      controller: [
              '$modalInstance',
      function($modalInstance) {
        console.info('Hello from the modal instance controller', $modalInstance);
      }]
    })
    .result.then(function(result) {
      console.info('modalInstance created', result);
    }, function(reason) {
      console.info('modalInstance aborted', reason);
    });
  }]);

  return pileModule;
});
