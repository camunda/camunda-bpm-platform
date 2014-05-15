'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define('camunda-tasklist', [
           'camunda-tasklist/rjsconf'
], function(rjsConf) {

  var tasklistApp;

  var appModules = rjsConf.shim['camunda-tasklist'];

  var deps = [
    'angular'
  ].concat(appModules);

  // converts AMD paths to angular module names
  // "camunda-tasklist/pile" will be "cam.tasklist.pile"
  function rj2ngNames(names) {
    var name, translated = [];
    for (var n = 0; n < names.length; n++) {
      name = (require(names[n]) || {}).name;
      if (name) translated.push(name);
    }
    return translated;
  }

  function loaded() {
    var angular = require('angular');
    var $ = angular.element;

    tasklistApp = angular.module('cam.tasklist', rj2ngNames(appModules));



    tasklistApp.controller('TasklistCtrl', [
             '$scope', '$rootScope', '$modal', 'camPileData',
    function ($scope,   $rootScope,   $modal,   camPileData) {
      $rootScope.batchActions = {
        selected: []
      };
      $rootScope.focusedPile = {};
      $rootScope.focusedTask = {};

      $scope.user = {
        id:   'max',
        name: 'Max Mustermann'
      };

      $scope.piles = [];

      $rootScope.$on('tasklist.pile.focused', function() {
        $('.task-board').removeClass('pile-edit');
        if ($rootScope.focusedPile) {
          $('.controls .focused-pile h5').text($rootScope.focusedPile.name || '&nbsp;');
        }
      });

      $scope.newPile = function() {
        $rootScope.focusedPile = {
          name: '',
          description: '',
          color: '',
          filters: []
        };

        $('.task-board').addClass('pile-edit');




        var newPileModalCtrl = [
                '$modalInstance',
        function($modalInstance) {
          console.info('Hello from the modal instance controller', $modalInstance);
        }];

        function created(result) {
          console.info('modalInstance created', result);
        }

        function aborted(reason) {
          console.info('modalInstance aborted', reason);
        }

        var modalInstance = $modal.open({
          // pass the current scope to the $modalInstance
          scope: $scope,

          size: 'lg',

          template: '<div>papi papo {{ user }}  {{ piles }}</div>',

          controller: newPileModalCtrl
        })
        .result.then(created, aborted);
      };

      camPileData.query({
        user: $scope.user
      }).then(function(piles) {
        $scope.piles = piles;
        $rootScope.focusedPile = $scope.piles[2];
        $rootScope.$emit('tasklist.pile.focused');
      }, function(err) {
        console.info('camPileData query error', err.stack);
      });
    }]);

    // with require.js, you need to bootstrap manually
    require(['domready'], function() {
      angular.bootstrap(document, ['cam.tasklist']);
    });
  }


  // configure require.js
  require.config(rjsConf);

  // and load the dependencies
  require(deps, loaded);

  return {
    deps:       deps,
    appModules: appModules,
    loaded:     loaded,
    rj2ngNames: rj2ngNames,
    rjsConf:    rjsConf
  };
});
