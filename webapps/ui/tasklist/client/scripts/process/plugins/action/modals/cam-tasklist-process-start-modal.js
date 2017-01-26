'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$rootScope',
  '$scope',
  '$translate',
  '$timeout',
  'debounce',
  'Notifications',
  'processData',
  'assignNotification',
  function(
    $rootScope,
    $scope,
    $translate,
    $timeout,
    debounce,
    Notifications,
    processData,
    assignNotification
  ) {
    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : ''),
          scope: $scope
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

    // setup ////////////////////////////////////////////////////////////////////////

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    var processStartData = processData.newChild($scope);

    // initially always reset the current selected process definition id to null
    processStartData.set('currentProcessDefinitionId', { id: null });

    var DEFAULT_OPTIONS = $scope.options = {
      hideCompleteButton : true,
      hideLoadVariablesButton: true,
      autoFocus : true,
      disableForm : false,
      disableAddVariableButton: false
    };

    $scope.PROCESS_TO_START_SELECTED = false;

    var query = null;

    var page = $scope.page = {
      total: 0,
      current: 1,
      searchValue: null
    };

    $scope.triggerOnStart = function() {};

    // observe /////////////////////////////////////////////////////////////////////////////////////

    processStartData.observe('processDefinitionQuery', function(_query) {
      query = angular.copy(_query);

      page.size = _query.maxResults;
      page.current = (_query.firstResult / page.size) + 1;
    });

    $scope.startFormState = processStartData.observe('startForm', function(startForm) {
      $scope.startForm = angular.copy(startForm);
    });

    $scope.processDefinitionState = processStartData.observe('processDefinitions', function(processDefinitions) {

      page.total = processDefinitions.count;

      $scope.processDefinitions = processDefinitions.items.sort(function(a, b) {
        // order by process definition name / key and secondary by tenant id
        var aName = (a.name || a.key).toLowerCase();
        var bName = (b.name || b.key).toLowerCase();

        var aTenantId = a.tenantId ? a.tenantId.toLowerCase() : '';
        var bTenantId = b.tenantId ? b.tenantId.toLowerCase() : '';

        if (aName < bName)
          return -1;
        else if (aName > bName)
          return 1;
        else if (aTenantId < bTenantId)
          return -1;
        else if (aTenantId > bTenantId)
          return 1;
        else
          return 0;
      });

      if(page.total > 0) {
        $timeout(function() {
          var element = document.querySelectorAll('div.modal-content ul.processes a')[0];
          if(element) {
            element.focus();
          }
        });
      }

    });

    // select process definition view //////////////////////////////////////////////////////

    $scope.pageChange = function() {
      query.firstResult = page.size * (page.current - 1);
      processStartData.set('processDefinitionQuery', query);
    };

    $scope.lookupProcessDefinitionByName = debounce(function() {
      var nameLike = page.searchValue;

      if (!nameLike) {
        delete query.nameLike;
      }
      else {
        query.nameLike = '%' + nameLike + '%';
      }

      // reset first result of query
      query.firstResult = 0;

      processStartData.set('processDefinitionQuery', query);


    }, 2000);

    $scope.selectProcessDefinition = function(processDefinition) {
      $scope.PROCESS_TO_START_SELECTED = true;

      var processDefinitionId = processDefinition.id;
      var processDefinitionKey = processDefinition.key;
      var deploymentId = processDefinition.deploymentId;

      $scope.options = angular.copy(DEFAULT_OPTIONS);

      $scope.params = {
        processDefinitionId : processDefinitionId,
        processDefinitionKey : processDefinitionKey,
        deploymentId : deploymentId
      };

      processStartData.set('currentProcessDefinitionId', {
        id: processDefinitionId
      });

    };

    // start a process view /////////////////////////////////////////////////////////////////

    $scope.$invalid = true;
    $scope.requestInProgress = false;

    $scope.$on('embedded.form.rendered', function() {
      $timeout(function() {
        var focusElement = document.querySelectorAll('.modal-body .form-container input')[0];
        if(focusElement) {
          focusElement.focus();
        }
      });
    });

    $scope.back = function() {
      $scope.$invalid = true;
      $scope.requestInProgress = false;
      $scope.PROCESS_TO_START_SELECTED = false;
      $scope.options = DEFAULT_OPTIONS;
      processStartData.set('currentProcessDefinitionId', { id: null });

      $timeout(function() {
        var element = document.querySelectorAll('div.modal-content ul.processes a')[0];
        if(element) {
          element.focus();
        }
      });
    };

    var executeAfterDestroy = [];
    $scope.$on('$destroy', function() {
      var job;
      while((job = executeAfterDestroy.pop())) {
        if(typeof job === 'function') {
          job();
        }
      }
    });

    // will be called when the form has been submitted
    $scope.completionCallback = function(err, result) {
      if (err) {
        $scope.requestInProgress = false;
        return errorNotification('PROCESS_START_ERROR', err);
      }

      executeAfterDestroy.push(function() {
        successNotification('PROCESS_START_OK');
        assignNotification({
          assignee: $rootScope.authentication.name,
          processInstanceId: result.id
        });
      });
      $scope.$close();
    };

    // will be called on initialization of the 'form'-directive
    $scope.registerCompletionHandler = function(fn) {
      // register a handler when a process should be started
      $scope.triggerOnStart = fn || function() {};
    };

    // will be triggered when the user select on 'Start'
    $scope.startProcessInstance = function() {
      $scope.requestInProgress = true;
      $scope.triggerOnStart();
    };

    // will be called the validation state has been changed
    $scope.notifyFormValidation = function(invalid) {
      $scope.$invalid = invalid;
    };

  }];
