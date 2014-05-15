'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'require', 'angular', 'moment', 'camunda-tasklist/pile/data'
], function(require, angular,   moment) {
  var pileModule = angular.module('cam.tasklist.pile', [
    'cam.tasklist.pile.data',
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  console.info('urls', require.toUrl('camunda-tasklist/pile/pile-tasks.html'));

  var modalUID = 0;

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

          $rootScope.focusedPile = scope.pile;
          $rootScope.$emit('tasklist.pile.focused');
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

      templateUrl: require.toUrl('camunda-tasklist/pile/pile-details.html')
    };
  }]);

  pileModule.directive('camPileTasks', [
          '$modal', '$rootScope', 'camPileData',
  function($modal,   $rootScope,   camPileData) {
    return {
      link: function(scope) {
        scope.now = new Date();
        scope.tasks = scope.tasks || [];
        scope.pile = scope.pile || $rootScope.focusedPile;

        scope.batchOperationSelect = function() {
          console.info('selected task', this);
        };

        $rootScope.$on('tasklist.pile.focused', function() {
          camPileData.query($rootScope.focusedPile, function(err, results) {
            if (err) {
              throw err;
            }

            console.info('tasklist.pile.focused tasks', results);
            $rootScope.focusedPile.tasks = scope.tasks = results;
          });
        });

        scope.focus = function(delta) {
          $rootScope.focusedTask = scope.tasks[delta];
          $rootScope.$emit('tasklist.task.focused');
        };
      },

      templateUrl: require.toUrl('camunda-tasklist/pile/pile-tasks.html')
    };
  }]);

  return pileModule;
});
