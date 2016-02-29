'use strict';

var fs = require('fs');

var uploadTemplate = fs.readFileSync(__dirname + '/variable-instance-upload-dialog.html', 'utf8');
var inspectTemplate = fs.readFileSync(__dirname + '/variable-instance-inspect-dialog.html', 'utf8');
var instancesTemplate = fs.readFileSync(__dirname + '/variable-instances-tab.html', 'utf8');

  module.exports = function(ngModule) {
    ngModule.controller('VariableInstancesController', [
              '$scope', '$sce', '$http', 'search', 'Uri', 'LocalExecutionVariableResource', 'Notifications', '$modal', '$q', 'camAPI',
      function($scope,   $sce,   $http,   search,   Uri,   LocalExecutionVariableResource,   Notifications,   $modal,   $q,   camAPI) {

        // input: processInstance, processData

        var variableInstanceData = $scope.processData.newChild($scope),
            processInstance = $scope.processInstance,
            variableInstanceIdexceptionMessageMap,
            variableCopies;

        var executionService = camAPI.resource('execution'),
            taskService = camAPI.resource('task');


        var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

        var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

        var filter = null;

        $scope.$watch('pages.current', function(newValue, oldValue) {
          if (newValue == oldValue) {
            return;
          }

          search('page', !newValue || newValue == 1 ? null : newValue);
        });

        variableInstanceData.observe([ 'filter', 'instanceIdToInstanceMap' ], function(newFilter, instanceIdToInstanceMap) {
          pages.current = newFilter.page || 1;

          updateView(newFilter, instanceIdToInstanceMap);
        });

        $scope.uploadVariable = function(info) {
          var promise = $q.defer();
          $modal.open({
            resolve: {
              variableInstance: function() { return info.variable; }
            },
            controller: 'VariableInstanceUploadController',
            template: uploadTemplate
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
              Notifications.addError({
                status: 'Variable',
                message: 'The variable \'' + info.variable.name + '\' could not be deleted successfully.',
                exclusive: true,
                duration: 5000
              });
              promise.reject();
            } else {
              Notifications.addMessage({
                status: 'Variable',
                message: 'The variable \'' + info.variable.name + '\' has been deleted successfully.',
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
            template: inspectTemplate,

            controller: 'VariableInstanceInspectController',

            windowClass: 'cam-widget-variable-dialog',

            resolve: {
              variableInstance: function () { return info.variable; }
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

        $scope.saveVariable = function (info) {
          var promise = $q.defer();
          var variable = info.variable;
          var modifiedVariable = {};

          var newValue = variable.value;//$scope.getCopy(variable.id).value;
          var newType = variable.type;//$scope.getCopy(variable.id).type;

          var newVariable = { value: newValue, type: newType };
          modifiedVariable[variable.name] = newVariable;

          var callback = function(error, data) {
            if(error) {
              Notifications.addError({
                status: 'Variable',
                message: 'The variable \'' + variable.name + '\' could not be changed successfully.',
                exclusive: true,
                duration: 5000
              });
              variableInstanceIdexceptionMessageMap[variable.id] = error.data;
              promise.reject();
            } else {
              Notifications.addMessage({
                status: 'Variable',
                message: 'The variable \'' + variable.name + '\' has been changed successfully.',
                duration: 5000
              });
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

        function updateView(newFilter, instanceIdToInstanceMap) {
          filter = $scope.filter = angular.copy(newFilter);

          delete filter.page;
          delete filter.activityIds;
          delete filter.scrollToBpmnElement;

          var page = pages.current,
              count = pages.size,
              firstResult = (page - 1) * count;

          var defaultParams = {
            processInstanceIdIn: [ processInstance.id ]
          };

          var pagingParams = {
            firstResult: firstResult,
            maxResults: count,
            deserializeValues: false
          };

          var params = angular.extend({}, filter, defaultParams);

          // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
          params.activityInstanceIdIn = params.activityInstanceIds;
          delete params.activityInstanceIds;

          $scope.variables = null;
          $scope.loadingState = 'LOADING';

          // get the 'count' of variables
          $http.post(Uri.appUri('engine://engine/:engine/variable-instance/count'), params).success(function(data) {
            pages.total = data.count;
          });

          variableInstanceIdexceptionMessageMap = {};
          variableCopies = {};

          $http.post(Uri.appUri('engine://engine/:engine/variable-instance/'), params, { params: pagingParams }).success(function(data) {

            $scope.variables = data.map(function (item) {
              var instance = instanceIdToInstanceMap[item.activityInstanceId];
              item.instance = instance;
              variableCopies[item.id] = angular.copy(item);

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
                    html:  '<a cam-select-activity-instance="\'' +
                                      instance.id +
                                      '\'" ng-href="#/process-instance/' +
                                      processInstance.id +
                                      '?detailsTab=variables-tab&activityInstanceIds=' +
                                      instance.id +
                                      '" title="' +
                                      instance.id +
                                      '">' +
                                        instance.name  +
                                      '</a>',
                    scopeVariables: {
                      processData: $scope.processData
                    }
                  }
                }
              };
            });

            $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
          });
        }

        $scope.getCopy = function (variableId) {
          var copy = variableCopies[variableId];
          if (isNull(copy)) {
            copy.type = 'String';
          }
          return copy;
        };

        var isNull = $scope.isNull = function (variable) {
          return variable.type === 'null' || variable.type === 'Null';
        };

        $scope.getBinaryVariableDownloadLink = function (variable) {
          return Uri.appUri('engine://engine/:engine/variable-instance/'+variable.id+'/data');
        };

        $scope.openUploadDialog = function (variableInstance) {
          $modal.open({
            resolve: {
              variableInstance: function() { return variableInstance; }
            },
            controller: 'VariableInstanceUploadController',
            template: uploadTemplate
          });
        };

        $scope.openInspectDialog = function (variableInstance) {
          $modal.open({
            resolve: {
              variableInstance: function() { return variableInstance; }
            },
            controller: 'VariableInstanceInspectController',
            template: inspectTemplate
          }).result.then(function() {
            variableInstanceData.set('filter', angular.copy($scope.filter));
          });
        };
      }]);

      var Configuration = function PluginConfiguration(ViewsProvider) {

        ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
          id: 'variables-tab',
          label: 'Variables',
          template: instancesTemplate,
          controller: 'VariableInstancesController',
          priority: 20
        });
      };

      Configuration.$inject = ['ViewsProvider'];
      ngModule.config(Configuration);
  };
