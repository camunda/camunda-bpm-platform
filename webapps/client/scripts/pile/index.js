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







  pileModule.controller('pilesCtrl', [
           '$scope', '$rootScope', '$timeout', 'camAPI',
  function ($scope,   $rootScope,   $timeout,   camAPI) {
    var Pile = camAPI.resource('pile');
    $scope.piles = [];
    $scope.loading = true;

    Pile.list({}, function(err, res) {
      $scope.loading = false;
      if (err) {
        throw err;
      }


      $timeout(function() {
        $scope.piles = res.items;
        $rootScope.currentPile = $scope.piles[0];
        $rootScope.$emit('tasklist.pile.current');
      }, 0);
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
      // controller: 'pilesCtrl',
      template: require('text!camunda-tasklist-ui/pile/list.html')
    };
  }]);




  pileModule.directive('camTasklistPileTasks', [
          '$modal', '$rootScope', '$timeout', 'camTasklistPileFilterConversion', 'camAPI',
  function($modal,   $rootScope,   $timeout,   camTasklistPileFilterConversion,   camAPI) {
    var Task = camAPI.resource('task');
    return {
      link: function(scope) {
        scope.pageSize = 15;
        scope.pageNum = 1;
        scope.totalItems = 0;

        scope.now = new Date();

        scope.loading = true;

        scope.tasks = scope.tasks || [];

        scope.pile = scope.pile || $rootScope.currentPile;

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

            $timeout(function() {
              scope.totalItems = res.count;
              scope.tasks = res.items;
            }, 0);
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

  return pileModule;
});
