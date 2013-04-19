"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var VariablesController = function($scope) {

    var variables = $scope.variables;


  };

  VariablesController.$inject = ["$scope"];

  module
    .controller("Variables", VariablesController);

});