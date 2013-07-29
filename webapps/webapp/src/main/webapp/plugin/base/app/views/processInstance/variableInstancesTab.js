ngDefine('cockpit.plugin.base.views', function(module) {

   function VariableInstancesController ($scope, $http, $location, $q, Uri, LocalExecutionVariableResource, RequestStatus) {

    // input: processInstanceId, selection, processInstance

    var pages = $scope.pages = { size: 50, total: 0 };

    var activityInstanceIds = null;
    var alreadyUpdated = false;

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

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
        updateView(currentPage);  
      }
      
    });

    $scope.$watch(function() { return $location.search().activityInstances; }, function (newValue) {
      activityInstanceIds = [];

      if (newValue && angular.isString(newValue)) {
        activityInstanceIds = newValue.split(',');
      } else if (newValue && angular.isArray(newValue)) {
        activityInstanceIds = newValue;
      }

      // always reset the current page to null
      $location.search('page', null);

      function waitForInstanceIdToInstanceMap() {
        var deferred = $q.defer();

        $scope.$watch('processInstance.instanceIdToInstanceMap', function (newValue) {
          if (newValue) {
            deferred.resolve(newValue);
          }
        });

        return deferred.promise;
      }

      if ($scope.processInstance.instanceIdToInstanceMap) {
        updateView(1);    
      } else {
        waitForInstanceIdToInstanceMap().then(function () {
          updateView(1);
        });
      }

    });

    function updateView(page) {
      $scope.variables = null;

      sequencer = 0;
      variableCopies = [];
      variablesInEditMode = [];
      
      var count = pages.size;
      var firstResult = (page - 1) * count;

      // get the 'count' of variables
      $http.post(Uri.appUri('engine://engine/:engine/variable-instance/count'), {
        processInstanceIdIn : [ $scope.processInstanceId ],
        activityInstanceIdIn :  activityInstanceIds
      })
      .success(function(data) {
        pages.total = Math.ceil(data.count / pages.size);
      });

      // get the variables
      $http.post(Uri.appUri('engine://engine/:engine/variable-instance/'), {
        processInstanceIdIn : [ $scope.processInstanceId ],
        activityInstanceIdIn :  activityInstanceIds
      }, {
        params: {firstResult: firstResult, maxResults: count}
      })
      .success(function(data) {
        var instanceIdToInstanceMap = $scope.processInstance.instanceIdToInstanceMap;
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

    $scope.selectActivityInstance = function (variable) {
      $scope.selection.view = {activityInstances: [ variable.instance ], scrollTo: variable.instance};
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

  module.controller('VariableInstancesController', [ '$scope', '$http', '$location', '$q', 'Uri', 'LocalExecutionVariableResource', 'RequestStatus', VariableInstancesController ]);

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
