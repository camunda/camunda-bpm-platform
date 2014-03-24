ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {

   function VariableInstancesController ($scope, $http, search, Uri, LocalExecutionVariableResource, Notifications, $dialog) {

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
      filter = angular.copy(newFilter);

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
        maxResults: count
      };

      var params = angular.extend({}, filter, defaultParams);

      // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
      params.activityInstanceIdIn = params.activityInstanceIds;
      delete params.activityInstanceIds;

      $scope.variables = null;

      // get the 'count' of variables
      $http.post(Uri.appUri('engine://engine/:engine/variable-instance/count'), params).success(function(data) {
        pages.total = Math.ceil(data.count / pages.size);
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
    };

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
    }

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

      LocalExecutionVariableResource.updateVariables({ executionId: variable.executionId }, { modifications : modifiedVariable }).$then(
        // success
        function(response) {
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
      return variable.type === 'binary' || variable.type === 'Binary';
    };

    var isSerializable = $scope.isSerializable = function (variable) {
      return variable.type === 'serializable' || variable.type === 'Serializable';
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
        isNull(variable)     
    };

    $scope.isEditable = function (param) {
      return !isSerializable(param) && !isBinary(param);
    };

    $scope.isDateValueValid = function (param) {
      console.log(param);
    };

    $scope.getBinaryVariableDownloadLink = function (variable) {
      return Uri.appUri('engine://engine/:engine/variable-instance/'+variable.id+'/data');
    }

    $scope.openUploadDialog = function (variableInstance) {
       var dialog = $dialog.dialog({
        resolve: {
          variableInstance: function() { return variableInstance; }
        },
        controller: 'VariableInstanceUpluadController',
        templateUrl: require.toUrl('./variable-instance-upload-dialog.html')
      });

      dialog.open().then(function(result) {
        // dialog closed.
      });
    }

    $scope.openInspectDialog = function (variableInstance) {
       var dialog = $dialog.dialog({
        resolve: {
          variableInstance: function() { return variableInstance; }
        },
        controller: 'VariableInstanceInspectController',
        templateUrl: require.toUrl('./variable-instance-inspect-dialog.html')
      });

      dialog.open().then(function(result) {
        // dialog closed.
      });
    }

  }

  module.controller('VariableInstancesController', [ '$scope', '$http', 'search', 'Uri', 'LocalExecutionVariableResource', 'Notifications', '$dialog', VariableInstancesController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'variables-tab',
      label: 'Variables',
      url: 'plugin://base/static/app/views/processInstance/variable-instances-tab.html',
      controller: 'VariableInstancesController',
      priority: 20
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
