'use strict';

var Ctrl = require('../components/delete');

module.exports = [
  '$scope',
  '$modalInstance',
function(
  $scope,
  $modalInstance
) {

  $scope.ctrl = new Ctrl();

}];
