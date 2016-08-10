'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-sorting-inputs.html', 'utf8');

module.exports = [
  '$translate',
  function(
    $translate
  ) {
    return {
      restrict: 'AC',

      replace: true,

      template: template,

      scope: {
        change: '=',
        applyHandler: '&',
        resetFunction: '=',
        variable: '='
      },

      controller: [
        '$scope',
        function(
        $scope
      ) {
          $scope.variableTypes = {
            'Boolean':  $translate.instant('BOOLEAN'),
            'Double':   $translate.instant('DOUBLE'),
            'Date':     $translate.instant('DATE'),
            'Integer':  $translate.instant('INTEGER'),
            'Long':     $translate.instant('LONG'),
            'Short':    $translate.instant('SHORT'),
            'String':   $translate.instant('STRING')
          };

          $scope.applySorting = function(evt) {
            $scope.applyHandler({$event: evt});
          };
        }]
    };
  }];
