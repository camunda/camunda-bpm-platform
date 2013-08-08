ngDefine('cockpit.plugin.base.views', function(module) {

   function VariableInstancesController ($scope, $http, search, Uri, LocalExecutionVariableResource, RequestStatus) {

    // input: processInstance, processData

    var variableInstanceData = $scope.processData.newChild($scope);
    var processInstance = $scope.processInstance;

    // contains the variable instances which are current
    // in the edit mode
    var variablesInEditMode = [];

    // contains for each failed request to save new value
    // of a variable instance the corresponding returned
    // exception
    var variableInstanceIdexceptionMessageMap = {};

    // this map contains for each shown variable instane
    // a copy from it
    var variableCopies = {};

    var sequencer = 0;

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

      $http.post(Uri.appUri('engine://engine/:engine/variable-instance/'), params, { params: pagingParams }).success(function(data) {

        angular.forEach(data, function(currentVariable) {
          var instance = instanceIdToInstanceMap[currentVariable.activityInstanceId];
          currentVariable.instance = instance;

          // set an internal id
          currentVariable.id = getNextId();

          // creates initially a copy of the current variable instance
          variableCopies[currentVariable.id] = angular.copy(currentVariable);
        });
        $scope.variables = data;
      });
    };

    /**
     * Returns the next id.
     */
    function getNextId () {
      return sequencer++;
    }

    $scope.editVariable = function (variable) {
      variablesInEditMode.push(variable);
    };

    $scope.isInEditMode = function (variable) {
      if (variablesInEditMode.indexOf(variable) === -1) {
        return false;
      }

      return true;
    };

    $scope.closeInPlaceEditing = function (variable) {
      var index = variablesInEditMode.indexOf(variable);
      variablesInEditMode.splice(index, 1);

      // clear the exception for the passed variable
      variableInstanceIdexceptionMessageMap[variable.id] = null;

      // reset the values of the copy
      var copy = $scope.getCopy(variable.id);
      copy.value = variable.value;
      copy.type = variable.type;
    };

    $scope.submit = function (variable) {
      RequestStatus.setBusy(true);

      var newValue = $scope.getCopy(variable.id).value;

      // If the value did not change then there is nothing to do!
      if (newValue === variable.value) {
        $scope.closeInPlaceEditing(variable);
        RequestStatus.setBusy(false);
        return;
      }

      var modifiedVariable = {};
      modifiedVariable[variable.name] = {value: newValue, type: variable.type};

      LocalExecutionVariableResource.updateVariables({ executionId: variable.executionId }, { modifications : modifiedVariable })
      .$then(
        // success
        function(response) {
          RequestStatus.setBusy(false);
          // Load the variable
          LocalExecutionVariableResource.get({ executionId: variable.executionId, localVariableName: variable.name })
          .$then(function (data) {
            variable.value = data.data.value;
            variable.type = data.data.type;

            $scope.closeInPlaceEditing(variable);
          })
      },
        // error
       function (error) {
        // set the exception
        RequestStatus.setBusy(false);
        variableInstanceIdexceptionMessageMap[variable.id] = error.data;
      });
    };

    $scope.getExceptionForVariableId = function (variableId) {
      return variableInstanceIdexceptionMessageMap[variableId];
    };

    $scope.getCopy = function (variableId) {
      return variableCopies[variableId];
    };

    var isBoolean = $scope.isBoolean = function (variable) {
      if (variable.value === true || variable.value === false) {
        return true;
      }
      return false;
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

    var isSerializable = $scope.isSerializable = function (variable) {
      return !isInteger(variable) &&
          !isShort(variable) &&
          !isLong(variable) &&
          !isDouble(variable) &&
          !isFloat(variable) &&
          !isString(variable) &&
          !isDate(variable) &&
          !isBoolean(variable) &&
          !isNull(variable);
    };

  };

  module.controller('VariableInstancesController', [ '$scope', '$http', 'search', 'Uri', 'LocalExecutionVariableResource', 'RequestStatus', VariableInstancesController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'variables-tab',
      label: 'Variables',
      url: 'plugin://base/static/app/views/processInstance/variable-instances-tab.html',
      controller: 'VariableInstancesController',
      priority: 15
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
