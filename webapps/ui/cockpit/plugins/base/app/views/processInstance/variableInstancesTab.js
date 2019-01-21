'use strict';

var angular = require('angular');
var fs = require('fs');

var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var variableInstancesTabSearchConfig = JSON.parse(fs.readFileSync(__dirname + '/variable-instances-tab-search-config.json', 'utf8'));

var instancesTemplate = fs.readFileSync(__dirname + '/variable-instances-tab.html', 'utf8');
var inspectTemplate = require('../../../../../client/scripts/components/variables/variable-inspect-dialog');
var uploadTemplate = require('../../../../../client/scripts/components/variables/variable-upload-dialog');


module.exports = function(ngModule) {
  ngModule.controller('VariableInstancesController', [
    '$scope', '$sce', 'search', 'Uri', 'LocalExecutionVariableResource',
    'Notifications', '$uibModal', '$q', 'camAPI', 'fixDate', 'unfixDate', '$translate', 'localConf',
    function($scope, $sce, search, Uri, LocalExecutionVariableResource,
      Notifications, $modal, $q, camAPI, fixDate, unfixDate, $translate,localConf) {

      // input: processInstance, processData
      var variableInstanceData = $scope.processData.newChild($scope),
          processInstance = $scope.processInstance,
          variableInstanceIdexceptionMessageMap,
          variableCopies;

      var executionService = camAPI.resource('execution'),
          taskService      = camAPI.resource('task'),
          variableService  = camAPI.resource('variable');

      // Table header
      $scope.headColumns = [
        { class: 'name',  request: 'variableName', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_NAME')},
        { class: 'type',  request: 'variableType', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_TYPE')},
        { class: 'value', request: '',             sortable: false, content: $translate.instant('PLUGIN_VARIABLE_VALUE')},
        { class: 'scope', request: '',             sortable: false, content: $translate.instant('PLUGIN_VARIABLE_SCOPE')}
      ];

      // Default sorting
      var defaultValue= { sortBy: 'variableName', sortOrder: 'asc' };
      $scope.sortObj   = loadLocal(defaultValue);

      $scope.searchConfig = angular.copy(variableInstancesTabSearchConfig);

      angular.forEach(variableInstancesTabSearchConfig.tooltips, function(translation, tooltip) {
        $scope.searchConfig.tooltips[tooltip] = $translate.instant(translation);
      });

      $scope.searchConfig.types.map(function(type) {
        type.id.value = $translate.instant(type.id.value);
        if (type.operators) {
          type.operators = type.operators.map(function(op) {
            op.value = $translate.instant(op.value);
            return op;
          });
        }
        return type;
      });

      variableInstanceData.observe('instanceIdToInstanceMap', function(instanceIdToInstanceMap) {
        $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      });

      $scope.onSearchChange = function(query, pages, sortObj) {
        $scope.query = query ||  $scope.query;
        $scope.pages = pages ||  $scope.pages;

        if ($scope.instanceIdToInstanceMap) {
          return updateView($scope.instanceIdToInstanceMap, $scope.query, $scope.pages, sortObj);
        }

        return $q.when($scope.total);
      };

      $scope.onSortChange = $scope.onSearchChange;


      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityInstanceIdIn');

      $scope.uploadVariable = function(info) {
        var promise = $q.defer();
        $modal.open({
          resolve: {
            basePath: function() { return getBasePath(info.variable); },
            variable: function() { return info.variable; }
          },
          controller: uploadTemplate.controller,
          template: uploadTemplate.template
        })
          .result.then(function() {
          // updated the variable, need to get the new data
          // reject the promise anyway
            promise.reject();

            // but then update the filter to force re-get of variables
            variableInstanceData.set('filter', angular.copy($scope.filter));
          }, function() {
          // did not update the variable, reject the promise
            promise.reject();
          });

        return promise.promise;
      };

      $scope.downloadVariable = function(info) {
        return Uri.appUri('engine://engine/:engine/variable-instance/' + info.variable.id +'/data');
      };

      $scope.deleteVariable = function(info) {
        var promise = $q.defer();

        var callback = function(error) {
          if(error) {

            var errorMessage = { text: '', payload: {}};

            if (error.response.body &&
              error.response.body.type === 'AuthorizationException') {
              errorMessage.text =  'PLUGIN_VARIABLE_INSTANCES_MESSAGES_ERROR_3';
              errorMessage.payload = {message: error.message};
            } else {
              errorMessage.text = 'PLUGIN_VARIABLE_INSTANCES_MESSAGES_ERROR_0';
              errorMessage.payload = {  name: info.variable.name };
            }


            Notifications.addError({
              status: $translate.instant('PLUGIN_VARIABLE_INSTANCES_STATUS_VARIABLE'),
              message: $translate.instant(errorMessage.text , errorMessage.payload),
              exclusive: true,
              duration: 5000
            });
            promise.reject();
          } else {
            Notifications.addMessage({
              status: $translate.instant('PLUGIN_VARIABLE_INSTANCES_STATUS_VARIABLE'),
              message: $translate.instant('PLUGIN_VARIABLE_INSTANCES_MESSAGES_ADD_0', { name: info.variable.name }),
              duration: 5000
            });
            promise.resolve(info.variable);
          }
        };

        if(info.original.taskId) {
          taskService.deleteVariable({
            id: info.original.taskId,
            varId: info.variable.name
          }, callback);
        } else {
          executionService.deleteVariable({
            id: info.variable.executionId,
            varId: info.variable.name
          }, callback);
        }

        return promise.promise;
      };

      $scope.editVariable = function(info) {
        var promise = $q.defer();

        $modal.open({
          template: inspectTemplate.template,

          controller: inspectTemplate.controller,

          windowClass: 'cam-widget-variable-dialog',

          resolve: {
            basePath: function() { return getBasePath(info.variable); },
            history: function() { return false; },
            readonly: function() { return false; },
            variable: function() { return info.variable; }
          }
        })
          .result.then(function() {
          // updated the variable, need to get the new data
          // reject the promise anyway
            promise.reject();

            // but then update the filter to force re-get of variables
            variableInstanceData.set('filter', angular.copy($scope.filter));
          }, function() {
          // did not update the variable, reject the promise
            promise.reject();
          });

        return promise.promise;
      };

      $scope.saveVariable = function(info) {
        var promise = $q.defer();
        var variable = info.variable;
        var modifiedVariable = {};

        var newValue = variable.value;
        var newType = variable.type;

        if(newType === 'Date') {
          newValue = fixDate(newValue);
        }

        var newVariable = { value: newValue, type: newType };
        modifiedVariable[variable.name] = newVariable;

        var callback = function(error) {
          if(error) {
            var errorMessage = { text: '', payload: {}};

            if (error.response.body  &&
                error.response.body.type === 'AuthorizationException') {
              errorMessage.text =  'PLUGIN_VARIABLE_INSTANCES_MESSAGES_ERROR_2';
              errorMessage.payload = {message: error.message};
            } else {
              errorMessage.text = 'PLUGIN_VARIABLE_INSTANCES_MESSAGES_ERROR_1';
              errorMessage.payload = { name: variable.name };
            }

            Notifications.addError({
              status: $translate.instant('PLUGIN_VARIABLE_INSTANCES_STATUS_VARIABLE'),
              message: $translate.instant(errorMessage.text , errorMessage.payload),
              exclusive: true,
              duration: 5000
            });
            variableInstanceIdexceptionMessageMap[variable.id] = error.data;
            promise.reject();
          } else {
            Notifications.addMessage({
              status: $translate.instant('PLUGIN_VARIABLE_INSTANCES_STATUS_VARIABLE'),
              message: $translate.instant('PLUGIN_VARIABLE_INSTANCES_MESSAGES_ADD_1', { name: variable.name}),
              duration: 5000
            });

            if(newVariable.type === 'Date') {
              newVariable.value = unfixDate(newVariable.value);
            }

            angular.extend(variable, newVariable);
            promise.resolve(info.variable);
          }
        };

        if(info.original.taskId) {
          taskService.modifyVariables({
            id: info.original.taskId,
            modifications: modifiedVariable
          }, callback);
        } else {
          executionService.modifyVariables({
            id: variable.executionId,
            modifications: modifiedVariable
          }, callback);
        }

        return promise.promise;
      };

      function getBasePath(variable) {
        return 'engine://engine/:engine/execution/' + variable.executionId + '/localVariables/' + variable.name;
      }

      function updateView(instanceIdToInstanceMap, variableQuery, pages, sortObj) {

        $scope.pagesObj = pages   || $scope.pagesObj ;
        sortObj         = sortObj || $scope.sortObj;

        var page        =  $scope.pagesObj.current,
            count       =  $scope.pagesObj.size,
            firstResult = (page - 1) * count;

        var defaultParams = {
          processInstanceIdIn: [ processInstance.id ]
        };

        // Add default sorting param
        if(sortObj) {
          defaultParams.sorting = [sortObj];
          saveLocal(sortObj);
        }

        var pagingParams = {
          firstResult    : firstResult,
          maxResults     : count,
          deserializeValues: false
        };
        var countParams = angular.extend({}, defaultParams, variableQuery);
        var params = angular.extend({}, pagingParams, defaultParams, variableQuery);

        $scope.variables = null;
        $scope.loadingState = 'LOADING';

        variableInstanceIdexceptionMessageMap = {};
        variableCopies = {};

        params = angular.extend({}, params, pagingParams);

        // get the 'count' of variables
        return variableService
          .count(countParams)
          .then(function(response) {
            $scope.total = response.count;
            // get variables objects
            return variableService
              .instances(params)
              .then(function(response) {
                var data = response;

                $scope.variables = data.map(function(item) {
                  var instance = instanceIdToInstanceMap[item.activityInstanceId];
                  item.instance = instance;
                  variableCopies[item.id] = angular.copy(item);

                  if(item.type === 'Date') {
                    item.value = unfixDate(item.value);
                  }

                  // prevents the list to throw an error when the activity instance is missing
                  var activityInstanceLink = '';
                  if(instance) {
                    activityInstanceLink = '<a ng-href="#/process-instance/' +
                      processInstance.id + '/runtime' +
                      '?detailsTab=variables-tab&'+ $scope.getSearchQueryForSearchType(instance.id) +
                      '" title="' +
                      instance.id +
                      '">' +
                      instance.name  +
                      '</a>';
                  }

                  return {
                    variable: {
                      id:           item.id,
                      name:         item.name,
                      type:         item.type,
                      value:        item.value,
                      valueInfo:    item.valueInfo,
                      executionId:  item.executionId
                    },
                    original: item,
                    additions: {
                      scope: {
                        html: activityInstanceLink,
                        scopeVariables: {
                          processData: $scope.processData
                        }
                      }
                    }
                  };
                });
                $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
                return $scope.total;
              });
          });
      }

      function saveLocal(sortObj) {
        localConf.set('sortHistVarInstTab', sortObj);

      }

      function loadLocal(defaultValue) {
        return localConf.get('sortHistVarInstTab', defaultValue);
      }


    }]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'variables-tab',
      label: 'PLUGIN_VARIABLE_INSTANCES_LABEL',
      template: instancesTemplate,
      controller: 'VariableInstancesController',
      priority: 20
    });
  };

  Configuration.$inject = ['ViewsProvider'];
  ngModule.config(Configuration);
};
