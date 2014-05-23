'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'require', 'angular', 'moment',
           'camunda-tasklist/pile/data',
           'text!camunda-tasklist/pile/form.html',
           'text!camunda-tasklist/pile/list.html',
           'text!camunda-tasklist/pile/details.html',
           'text!camunda-tasklist/pile/tasks.html'
], function(require,   angular,   moment) {
  var pileModule = angular.module('cam.tasklist.pile', [
    'cam.tasklist.pile.data',
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  var modalUID = 0;
  var $ = angular.element;





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

      template: require('text!camunda-tasklist/pile/details.html')
    };
  }]);




  pileModule.directive('camTasklistPiles', [
          '$modal', '$rootScope',
  function($modal,   $rootScope) {
    return {
      template: require('text!camunda-tasklist/pile/list.html')
    };
  }]);




  pileModule.directive('camTasklistPileTasks', [
          '$modal', '$rootScope', 'camPileData',
  function($modal,   $rootScope,   camPileData) {
    return {
      link: function(scope) {
        scope.now = new Date();
        scope.tasks = scope.tasks || [];
        scope.pile = scope.pile || $rootScope.currentPile;

        scope.batchOperationSelect = function() {
          console.info('selected task', this);
        };

        // $rootScope.$on('tasklist.pile.current', function() {
        $rootScope.$watch('currentPile', function() {
          if (!$rootScope.currentPile) {
            return;
          }

          camPileData.tasks($rootScope.currentPile).then(function(results) {
            console.info('tasklist.pile.current tasks', results);
            $rootScope.currentPile.tasks = scope.tasks = results;
          }, function(err) {
            console.warn('tasklist.pile.current tasks', err);
          });
        });



        scope.focus = function(delta) {
          $rootScope.currentTask = scope.tasks[delta];
          $rootScope.$emit('tasklist.task.current');
        };
      },

      template: require('text!camunda-tasklist/pile/tasks.html')
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
    console.warn('Should open a modal window with new pile form.');
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

      template: require('text!camunda-tasklist/pile/form.html'),

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
