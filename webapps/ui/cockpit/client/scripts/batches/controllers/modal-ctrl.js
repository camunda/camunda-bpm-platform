'use strict';

var Ctrl = require('../components/delete');

module.exports = [
  '$scope',
  function(
  $scope
) {

    $scope.ctrl = new Ctrl();

  }];
