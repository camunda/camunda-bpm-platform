'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var Ctrl = require('../components/batch.js');
var EventEmitter = require('events');

module.exports = [
  '$scope',
  'page',
  'camAPI',
  '$location',
function(
  $scope,
  page,
  camAPI,
  $location
) {

  var events = new EventEmitter();
  $scope.$on('$destroy', function() {
    events.removeAllListeners();
  });

  $scope.$watch(function() {
    return ($location.search() || {});
  }, function(newValue) {
    if(newValue.details && newValue.type) {
      $scope.ctrl.loadDetails(newValue.details, newValue.type);
    }
  });

  require('../components/breadcrumbs')(page, $scope.$root);

  $scope.ctrl = new Ctrl(camAPI, events);
  $scope.ctrl.load();
}];
