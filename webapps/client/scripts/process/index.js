'use strict';


define([
           'angular', 'angular-bootstrap',
           'camunda-tasklist-ui/api',
           'text!camunda-tasklist-ui/process/start.html'
], function(angular) {

  var processModule = angular.module('cam.tasklist.process', [
    'cam.tasklist.client',
    'ui.bootstrap'
  ]);



  processModule.controller('processStartModalFormCtrl', [
    '$scope',
    '$q',
    'camAPI',
    'CamForm',
    'Notifications',
  function(
    $scope,
    $q,
    camAPI,
    CamForm,
    Notifications
  ) {
    var $ = angular.element;

    var ProcessDefinition = camAPI.resource('process-definition');

    var emptyVariable = {
      name:   '',
      value:  '',
      type:   ''
    };


    // http://docs.camunda.org/latest/api-references/rest/#process-definition-start-process-instance-method
    // Valid variable values are Boolean, Number, String and Date values.
    // NOTE: Actually... forget the docs...
    var variableTypes = {
      'Boolean':  'checkbox',
      'Integer':  'text',
      'Double':   'text',
      'Long':     'text',
      'Short':    'text',
      'String':   'text',
      'Date':     'datetime'
    };

    function close(result) {
      var isFunction = angular.isFunction($scope.$close);

      var cb =  isFunction ?
                $scope.$close :
                angular.noop;

      cb(result);
    }

    // function loadError(err) {
    //   $scope.loadingProcesses = false;
    // }

    // used by the pagination directive
    $scope.currentPage = 1;

    $scope.itemsPerPage = 25;

    $scope.totalProcesses = 0;

    $scope.processes = [];

    $scope.variables = [];

    $scope.variableTypes = variableTypes;

    $scope.loadingProcesses = false;

    $scope.close = close;


    $scope.selected = function($item) {
      $scope.getStartForm($item);
    };


    $scope.lookupProcess = function(val) {
      var deferred = $q.defer();

      $scope.loadingProcesses = true;

      ProcessDefinition.list({
        nameLike: '%'+ val +'%'
      }, function(err, res) {
        $scope.loadingProcesses = false;

        if (err) {
          return deferred.reject(err);
        }

        deferred.resolve(res.items);
      });

      return deferred.promise;
    };


    $scope.showList = function() {
      $scope.startingProcess = null;
    };


    $scope.loadProcesses = function() {
      $scope.loadingProcesses = true;
      var where = {};

      // I found that in the REST API documentation,
      // I supposed it was aimed to be used,
      // but using it lead to empty results
      // where.startableBy = camStorage.get('user').id;

      where.maxResults = where.maxResults || $scope.itemsPerPage;

      ProcessDefinition.list(where, function(err, res) {
        $scope.loadingProcesses = false;
        if (err) {
          Notifications.addError(err);
          throw err;
        }

        $scope.totalProcesses = res.count;

        $scope.processes = res.items.sort(function(a, b) {
          var aName = (a.name || a.key).toLowerCase();
          var bName = (b.name || b.key).toLowerCase();
          if (aName < bName)
             return -1;
          if (aName > bName)
            return 1;
          return 0;
        });
        // $scope.$apply(function() {
        // });
      });
    };
    $scope.loadProcesses();


    $scope.getStartForm = function(startingProcess) {
      $scope.startingProcess = startingProcess;
      $scope.loadingProcesses = true;

      ProcessDefinition.startForm({
        id: startingProcess.id
      }, function(err, result) {
        $scope.loadingProcesses = false;

        if (err) {
          Notifications.addError(err);
          throw err;
        }

        if (result.key) {
          var parts = result.key.split('embedded:');
          var ctx = result.contextPath;
          var formUrl;

          if (parts.length > 1) {
            formUrl = parts.pop();
            // ensure a trailing slash
            ctx = ctx + (ctx.slice(-1) !== '/' ? '/' : '');
            // formUrl = formUrl[0] === '/' ? formUrl.slice(1) : formUrl;
            formUrl = formUrl.replace(/app:(\/?)/, ctx);
          }
          else {
            formUrl = result.key;
          }

          var form = new CamForm({
            processDefinitionId:  startingProcess.id,
            containerElement:     $('.start-form-container'),
            client:               camAPI,
            formUrl:              formUrl
          });

          $scope.startingProcess._form = form;
        }
        else {
          // generic form
        }
      });
      // $scope.variables = [];
      // $scope.addVariable();
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


    $scope.submitForm = function() {
      if (!$scope.startingProcess || !$scope.startingProcess.key) {
        return false;
      }

      function submitCb(err, res) {
        if (err) {
          Notifications.addError(err);
          throw err;
        }

        Notifications.addMessage({
          text: 'The process has been started.'
        });
        close(res);
      }

      var vars = {};
      if (!$scope.startingProcess._form) {
        angular.forEach($scope.variables, function(val) {
          if (val.name[0] !== '$' && val.name) {
            vars[val.name] = {type: val.type, value: val.value};
          }
        });

        ProcessDefinition.submit({
          key: $scope.startingProcess.key,
          variables: vars
        }, submitCb);
      }
      else {
        $scope.startingProcess._form.submit(submitCb);
      }
    };
  }]);


  processModule.controller('processStartCtrl', [
          '$modal', '$scope',
  function($modal,   $scope) {
    $scope.openProcessStartModal = function() {
      $modal.open({
        size: 'lg',
        controller: 'processStartModalFormCtrl',
        template: require('text!camunda-tasklist-ui/process/start.html')
      });
    };
  }]);

  return processModule;
});
