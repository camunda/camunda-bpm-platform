'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'angular-bootstrap',
           'camunda-tasklist/process/data',
           'text!camunda-tasklist/process/start.html'
], function(angular) {

  var processModule = angular.module('cam.tasklist.process', [
    'cam.tasklist.process.data',
    'ui.bootstrap'
  ]);



  processModule.controller('processStartModalFormCtrl', [
          '$scope', 'camLegacyProcessData',
  function($scope,   camLegacyProcessData) {
    var emptyVariable = {
      name:   '',
      value:  '',
      type:   ''
    };

    // http://docs.camunda.org/latest/api-references/rest/#process-definition-start-process-instance-method
    // Valid variable values are Boolean, Number, String and Date values.
    var variableTypes = {
      'Boolean':  'checkbox',
      // 'Number':   'number',
      'Number':   'text',
      'String':   'text',
      'Date':     'datetime'
    };

    function close(result) {
      var isFunction = $scope.$parent &&
                       $scope.$parent.$parent &&
                       angular.isFunction($scope.$parent.$parent.$close);

      var cb =  isFunction ?
                $scope.$parent.$parent.$close :
                angular.noop;

      cb(result);
    }

    $scope.processes = [];

    $scope.totalProcesses = 0;

    $scope.variables = [];

    $scope.variableTypes = variableTypes;

    $scope.loadingProcesses = false;

    function loadError(err) {
      console.warn('loading error', err.stack ? err.stack : err);
      $scope.loadingProcesses = false;
    }

    $scope.getProcess = function(val) {
      $scope.loadingProcesses = true;
      camLegacyProcessData.list().then(function(res){
        $scope.loadingProcesses = false;
        $scope.processes = res;
      }, loadError);
    };


    $scope.addVariable = function() {
      $scope.variables.push(angular.copy(emptyVariable));
    };
    $scope.addVariable();


    $scope.removeVariable = function(delta) {
      var vars = [];

      angular.forEach($scope.variables, function(variable, d) {
        if (d != delta) {
          vars.push(variable);
        }
      });

      $scope.variables = vars;
    };


    $scope.getStartForm = function(startingProcess) {
      $scope.startingProcess = startingProcess;
      $scope.variables = [];
      $scope.addVariable();
      // camLegacyProcessData.getForm(startingProcess.key).then(function() {});
    };


    $scope.showList = function() {
      $scope.startingProcess = null;
    };

    $scope.loadProcesses = function() {
      $scope.loadingProcesses = true;
      var where = {};

      camLegacyProcessData.count(where).then(function(result) {
        $scope.totalProcesses = result.count;

        camLegacyProcessData.list(where).then(function(processes) {
          $scope.loadingProcesses = false;

          $scope.processes = processes;
        }, loadError);
      }, loadError);
    };
    $scope.loadProcesses();

    $scope.submitForm = function(htmlForm) {
      var vars = {};

      angular.forEach($scope.variables, function(val) {
        if (val.name[0] !== '$') {
          vars[val.name] = {type: val.type, value: val.value};
        }
      });

      camLegacyProcessData.start($scope.startingProcess.key, {
        data: {
          variables: vars
        }
      }).then(function(result) {
        close(result);
      }, loadError);
    };
  }]);


  processModule.controller('processStartCtrl', [
          '$modal', '$scope', '$rootScope',
  function($modal,   $scope,   $rootScope) {
    $modal.open({
      size: 'lg',
      scope: $scope,
      template: require('text!camunda-tasklist/process/start.html')
    });
  }]);

  return processModule;
});
