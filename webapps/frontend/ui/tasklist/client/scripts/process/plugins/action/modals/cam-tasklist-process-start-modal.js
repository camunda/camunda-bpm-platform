/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

var DEFAULT_OPTIONS = {
  hideCompleteButton: true,
  hideStartButton: false,
  hideLoadVariablesButton: true,
  autoFocus: true,
  disableForm: false,
  disableAddVariableButton: false
};

module.exports = [
  '$rootScope',
  '$scope',
  '$translate',
  '$timeout',
  'debounce',
  'Notifications',
  'processData',
  'assignNotification',
  '$location',
  'search',
  'camAPI',
  function(
    $rootScope,
    $scope,
    $translate,
    $timeout,
    debounce,
    Notifications,
    processData,
    assignNotification,
    $location,
    search,
    camAPI
  ) {
    $scope.$on('authentication.login.required', function() {
      $scope.$dismiss();
    });

    function errorNotification(src, err) {
      $translate(src)
        .then(function(translated) {
          Notifications.addError({
            status: translated,
            message: err ? err.message : '',
            scope: $scope
          });
        })
        .catch(angular.noop);
    }

    function successNotification(src) {
      $translate(src)
        .then(function(translated) {
          Notifications.addMessage({
            duration: 3000,
            status: translated
          });
        })
        .catch(angular.noop);
    }

    // setup ////////////////////////////////////////////////////////////////////////

    var processStartData = processData.newChild($scope);

    // initially always reset the current selected process definition id to null
    processStartData.set('currentProcessDefinitionId', {id: null});

    $scope.options = angular.copy(DEFAULT_OPTIONS);

    $scope.PROCESS_TO_START_SELECTED = false;

    var query = null;

    var page = ($scope.page = {
      total: 0,
      current: 1,
      searchValue: null
    });

    $scope.triggerOnStart = function() {};

    // observe /////////////////////////////////////////////////////////////////////////////////////

    processStartData.observe('processDefinitionQuery', function(_query) {
      query = angular.copy(_query);

      page.size = _query.maxResults;
      page.current = _query.firstResult / page.size + 1;
    });

    $scope.startFormState = processStartData.observe('startForm', function(
      startForm
    ) {
      $scope.startForm = angular.copy(startForm);
    });

    $scope.processDefinitionState = processStartData.observe(
      'processDefinitions',
      function(processDefinitions) {
        page.total = processDefinitions.count;

        $scope.processDefinitions = processDefinitions.items.sort(function(
          a,
          b
        ) {
          // order by process definition name / key and secondary by tenant id
          var aName = (a.name || a.key).toLowerCase();
          var bName = (b.name || b.key).toLowerCase();

          var aTenantId = a.tenantId ? a.tenantId.toLowerCase() : '';
          var bTenantId = b.tenantId ? b.tenantId.toLowerCase() : '';

          if (aName < bName) return -1;
          else if (aName > bName) return 1;
          else if (aTenantId < bTenantId) return -1;
          else if (aTenantId > bTenantId) return 1;
          else return 0;
        });

        if (page.total > 0) {
          $timeout(function() {
            var element = document.querySelectorAll(
              'div.modal-content ul.processes a'
            )[0];
            if (element) {
              element.focus();
            }
          });
        }
      }
    );

    // select process definition view //////////////////////////////////////////////////////

    $scope.pageChange = function() {
      query.firstResult = page.size * (page.current - 1);
      processStartData.set('processDefinitionQuery', query);
    };

    $scope.lookupProcessDefinitionByName = debounce(function() {
      var nameLike = page.searchValue;

      if (!nameLike) {
        delete query.nameLike;
      } else {
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
      var processDefinitionName = processDefinition.name;

      $scope.options = angular.copy(DEFAULT_OPTIONS);

      $scope.params = {
        processDefinitionId: processDefinitionId,
        processDefinitionKey: processDefinitionKey,
        deploymentId: deploymentId,
        processDefinitionName: processDefinitionName
      };

      var searchData = {processStart: processDefinitionKey};
      if (processDefinition.tenantId) {
        searchData.processTenant = processDefinition.tenantId;
      }

      search.updateSilently(searchData);

      processStartData.set('currentProcessDefinitionId', {
        id: processDefinitionId
      });
    };

    var processToStart = $location.search()['processStart'];
    if (processToStart && typeof processToStart === 'string') {
      var processQuery = {
        key: processToStart,
        latest: true,
        active: true,
        startableInTasklist: true,
        startablePermissionCheck: true,
        maxResults: 1
      };

      var tenantId = $location.search()['processTenant'];
      if (tenantId) {
        processQuery.tenantIdIn = tenantId;
      }

      camAPI
        .resource('process-definition')
        .list(processQuery, function(err, res) {
          if (err || res.items.length === 0) {
            return err;
          }

          $scope.selectProcessDefinition(res.items[0]);
        });
    }

    // start a process view /////////////////////////////////////////////////////////////////

    $scope.$invalid = true;
    $scope.requestInProgress = false;

    $scope.$on('embedded.form.rendered', function() {
      $timeout(function() {
        var focusElement = document.querySelectorAll(
          '.modal-body .form-container input'
        )[0];
        if (focusElement) {
          focusElement.focus();
        }
      });
    });

    $scope.back = function() {
      $scope.$invalid = true;
      $scope.requestInProgress = false;
      $scope.PROCESS_TO_START_SELECTED = false;
      $scope.options = DEFAULT_OPTIONS;
      processStartData.set('currentProcessDefinitionId', {id: null});

      $timeout(function() {
        var element = document.querySelectorAll(
          'div.modal-content ul.processes a'
        )[0];
        if (element) {
          element.focus();
        }
      });
    };

    var executeAfterDestroy = [];
    $scope.$on('$destroy', function() {
      var job;
      while ((job = executeAfterDestroy.pop())) {
        if (typeof job === 'function') {
          job();
        }
      }
    });

    // will be called when the form has been submitted
    $scope.completionCallback = function(err, result) {
      if (err) {
        $scope.requestInProgress = false;

        if (err.message !== 'camForm submission prevented') {
          errorNotification('PROCESS_START_ERROR', err);
        }

        return;
      }

      executeAfterDestroy.push(function() {
        successNotification('PROCESS_START_OK');
        assignNotification({
          assignee: $rootScope.authentication.name,
          processInstanceId: result.id,
          maxResults: 15
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
  }
];
