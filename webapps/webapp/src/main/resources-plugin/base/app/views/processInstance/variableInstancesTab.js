/* global define: false, angular: false */
define(['angular', 'text!./variable-instance-upload-dialog.html', 'text!./variable-instance-inspect-dialog.html', 'text!./variable-instances-tab.html'],
function(angular, uploadTemplate, inspectTemplate, instancesTemplate) {
  'use strict';

  return function(ngModule) {
    ngModule.controller('VariableInstancesController', [
              '$scope', '$http', 'search', 'Uri', 'LocalExecutionVariableResource', 'Notifications', '$modal',
      function($scope,   $http,   search,   Uri,   LocalExecutionVariableResource,   Notifications,   $modal) {

        // input: processInstance, processData

        var variableInstanceData = $scope.processData.newChild($scope),
            processInstance = $scope.processInstance,
            variableInstanceIdexceptionMessageMap,
            variableCopies;

        $scope.variableTypes = [
                              'String',
                              'Boolean',
                              'Short',
                              'Integer',
                              'Long',
                              'Double',
                              'Date'
                            ];

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

          // get the 'count' of variables
          $http.post(Uri.appUri('engine://engine/:engine/variable-instance/count'), params).success(function(data) {
            pages.total = data.count;
          });

          variableInstanceIdexceptionMessageMap = {};
          variableCopies = {};

          $http.post(Uri.appUri('engine://engine/:engine/variable-instance/'), params, { params: pagingParams }).success(function(data) {

            angular.forEach(data, function(currentVariable) {
              var instance = instanceIdToInstanceMap[currentVariable.activityInstanceId];
              currentVariable.instance = instance;

              // creates initially a copy of the current variable instance
              variableCopies[currentVariable.id] = angular.copy(currentVariable);
            });
            $scope.variables = data;
          });
        }

        $scope.editVariable = function (variable) {
          variable.inEditMode = true;
        };

        $scope.closeInPlaceEditing = function (variable) {
          delete variable.inEditMode;

          // clear the exception for the passed variable
          variableInstanceIdexceptionMessageMap[variable.id] = null;

          // reset the values of the copy
          var copy = $scope.getCopy(variable.id);
          copy.value = variable.value;
          copy.type = variable.type;
        };

        var isValid = $scope.isValid = function (form) {
          return !form.$invalid;
        };

        $scope.submit = function (variable, form) {
          if (!isValid(form)) {
            return;
          }

          var newValue = $scope.getCopy(variable.id).value,
              newType = $scope.getCopy(variable.id).type;

          // If the value did not change then there is nothing to do!
          if (newValue === variable.value && newType === variable.type) {
            $scope.closeInPlaceEditing(variable);
            return;
          }

          var modifiedVariable = {};
          var newVariable = { value: newValue, type: newType };
          modifiedVariable[variable.name] = newVariable;

          LocalExecutionVariableResource.updateVariables({ executionId: variable.executionId }, { modifications : modifiedVariable }).$promise.then(
            // success
            function() {
              Notifications.addMessage({ status: 'Variable', message: 'The variable \'' + variable.name + '\' has been changed successfully.', duration: 5000 });
              angular.extend(variable, newVariable);
              $scope.closeInPlaceEditing(variable);
            },
            // error
            function (error) {
              // set the exception
              Notifications.addError({ status: 'Variable', message: 'The variable \'' + variable.name + '\' could not be changed successfully.', exclusive: true, duration: 5000 });
              variableInstanceIdexceptionMessageMap[variable.id] = error.data;
            });
        };

        $scope.getExceptionForVariableId = function (variableId) {
          return variableInstanceIdexceptionMessageMap[variableId];
        };

        $scope.getCopy = function (variableId) {
          var copy = variableCopies[variableId];
          if (isNull(copy)) {
            copy.type = 'String';
          }
          return copy;
        };

        var isBoolean = $scope.isBoolean = function (variable) {
          return variable.type === 'boolean' || variable.type === 'Boolean';
        };

        var isInteger = $scope.isInteger = function (variable) {
          return variable.type === 'integer' || variable.type === 'Integer';
        };

        var isShort = $scope.isShort = function (variable) {
          return variable.type === 'short' || variable.type === 'Short';
        };

        var isLong = $scope.isLong = function (variable) {
          return variable.type === 'long' || variable.type === 'Long';
        };

        var isDouble = $scope.isDouble = function (variable) {
          return variable.type === 'double' || variable.type === 'Double';
        };

        var isFloat = $scope.isFloat = function (variable) {
          return variable.type === 'float' || variable.type === 'Float';
        };

        var isString = $scope.isString = function (variable) {
          return variable.type === 'string' || variable.type === 'String';
        };

        var isDate = $scope.isDate = function (variable) {
          return variable.type === 'date' || variable.type === 'Date';
        };

        var isNull = $scope.isNull = function (variable) {
          return variable.type === 'null' || variable.type === 'Null';
        };

        var isBinary = $scope.isBinary = function (variable) {
          return variable.type === 'bytes' || variable.type === 'Bytes';
        };

        var isObject = $scope.isObject = function (variable) {
          return variable.type === 'object' || variable.type === 'Object';
        };

        var isPrimitive = $scope.isPrimitive = function (variable) {
          return isInteger(variable) ||
            isShort(variable) ||
            isLong(variable) ||
            isDouble(variable) ||
            isFloat(variable) ||
            isString(variable) ||
            isDate(variable) ||
            isBoolean(variable) ||
            isNull(variable);
        };

        $scope.isEditable = function (param) {
          return !isObject(param) && !isBinary(param);
        };

        $scope.isDateValueValid = function (param) {
          // console.log(param);
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


});
