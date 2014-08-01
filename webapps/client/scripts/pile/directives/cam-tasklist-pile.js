define([
  'text!./cam-tasklist-pile.html'
], function(
  template
) {
  'use strict';
  return [
    '$modal',
    '$rootScope',
  function(
    $modal,
    $rootScope
  ) {
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

      template: template
    };
  }];
});
