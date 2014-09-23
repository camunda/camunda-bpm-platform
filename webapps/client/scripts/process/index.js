'use strict';


define([
  'angular',
  'text!camunda-tasklist-ui/process/start.html',
  'camunda-tasklist-ui/api',
  'angular-bootstrap'
], function(
  angular,
  templateStartForm
) {

  var processModule = angular.module('cam.tasklist.process', [
    'cam.tasklist.client',
    'ui.bootstrap'
  ]);



  processModule.controller('processStartModalFormCtrl', [
    '$rootScope',
    '$scope',
    '$q',
    '$translate',
    'camAPI',
    'CamForm',
    'Notifications',
  function(
    $rootScope,
    $scope,
    $q,
    $translate,
    camAPI,
    CamForm,
    Notifications
  ) {
    var $ = angular.element;



    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : '')
        });
      });
    }

    function successNotification(src) {
      $translate(src).then(function(translated) {
        Notifications.addMessage({
          duration: 3000,
          status: translated
        });
      });
    }


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
        suspended: false,
        latest: true,
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
      $scope.variables = [];
    };


    $scope.loadProcesses = function() {
      $scope.loadingProcesses = true;
      var where = {
        latest: true
      };

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

        // stop here if there is no "embedded form"
        if (!result.key) { return; }

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
      });
    };


    $scope.addVariable = function() {
      $scope.variables.push(angular.copy(emptyVariable));
    };
    // $scope.addVariable();


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
          errorNotification('PROCESS_START_ERROR', err);
          throw err;
        }

        successNotification('PROCESS_START_OK');
        $rootScope.$broadcast('tasklist.process.start');
        close(res);
      }

      if ($scope.startingProcess._form) {
        $scope.startingProcess._form.submit(submitCb);
      }
      else {
        var data = {};
        data.id = $scope.startingProcess.id;
        data.variables = {};
        angular.forEach($scope.variables, function(variable) {
          data.variables[variable.name] = {
            type: variable.type,
            value: variable.value
          };
        });
        ProcessDefinition.submitForm(data, submitCb);
      }
    };
  }]);


  processModule.controller('processStartCtrl', [
    '$modal',
    '$scope',
    '$rootScope',
  function(
    $modal,
    $scope,
    $rootScope
  ) {
    var modalInstance;

    function clearModalInstance() {
      modalInstance = null;
    }

    $scope.openProcessStartModal = function() {
      if (!$rootScope.authentication || !$rootScope.authentication.name) {
        return;
      }

      modalInstance = $modal.open({
        size: 'lg',
        controller: 'processStartModalFormCtrl',
        template: templateStartForm
      });

      modalInstance.result.then(clearModalInstance, clearModalInstance);

      $rootScope.$on('authentication.session.expired', function() {
        if (modalInstance) {
          modalInstance.close();
        }
      });
    };
  }]);

  return processModule;
});
