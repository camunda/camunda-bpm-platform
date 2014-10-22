define([
  'angular',
  'camunda-bpm-sdk'
], function(
  angular,
  camSDK
) {
  'use strict';
  return [
    '$scope',
    '$rootScope',
    '$timeout',
  function(
    $scope,
    $rootScope,
    $timeout
  ) {
    function parseSearch(search, query) {
      var searchRegEx = /^\s*(.*?)\s*(!=|<=|>=|like|[=<>])\s*(.*?)\s*$/;
      var match = searchRegEx.exec(query);
      if(match && match.length === 4) {
        search.name = match[1];
        search.operator = match[2];
        search.value = match[3];
        search.operators = getOperators(getType(parseValue(search.value)));
        return true;
      }
      return false;
    }
    function createSearchObj(type) {
      return {
        type : type,
        operator: "=",
        operators: getOperators()
      };
    }
    function getOperators(varType) {
      switch(varType) {
        case 'date':    return ["<", ">", "<=", ">="];
        case 'boolean':
        case 'object':  return ["=", "!="];
        case 'number':  return ["=", "!=", "<", ">", "<=", ">="];
        default:        return ["=", "!=", "<", ">", "<=", ">=", "like"];
      }
    }

    var dateRegex = /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/;
    function getType(value) {
      if(value && typeof value === "string" && value.match(dateRegex)) {
        return "date";
      }
      return typeof value;
    }

    $scope.types = ["Process Variable", "Task Variable", "Case Variable"];
    $scope.dropdownOpen = false;

    $scope.deleteSearch = function(idx) {
      $scope.searches.splice(idx,1);
      updateQuery();
    };

    $scope.createSearch = function(type){
      var search = createSearchObj(type);
      if(!parseSearch(search, $scope.inputQuery)) {
        search.value = $scope.inputQuery;
        search.operators = getOperators(getType(parseValue(search.value)));
      }
      $scope.searches.push(search);
      updateQuery();

      // need to use timeout, because jQuery initiates an apply cycle
      // while the current apply cycle is still in progress
      $timeout(function(){angular.element('.search-container > input').blur();});
      $scope.dropdownOpen = false;
      $scope.inputQuery = "";
    };

    function getDefaultOperator(valueType) {
      switch(valueType) {
        case 'date': return ">=";
        default:     return "=";
      }
    }

    $scope.changeSearch = function(idx, field, value) {
      var search = $scope.searches[idx];
      search[field] = value;
      var valueType = getType(parseValue(search.value));
      search.operators = getOperators(valueType);
      if(search.operators.indexOf(search.operator) === -1) {
        // if the current value type does not allow the selected operator,
        // fall back to default operator
        search.operator = getDefaultOperator(valueType);
      }
      updateQuery();
    };

    $scope.selectType = function(type) {
      $scope.createSearch(type);
    };

    $scope.onFocus = function(){
      $scope.dropdownOpen = true;
    };

    $scope.onBlur = function(){
      $scope.dropdownOpen = false;
    };

    $scope.dropdownHandler = function(evt) {
      evt.preventDefault();
    };

    var operatorTable = {
      "<" : "lt",
      ">" : "gt",
      "=" : "eq",
      "!=": "neq",
      ">=": "gteq",
      "<=": "lteq",
      "like":"like"
    };
    var typeTable = {
      "Process Variable" : "processVariables",
      "Task Variable" : "taskVariables",
      "Case Variable" : "caseInstanceVariables"
    };
    function isValid(search) {
      return $scope.types.indexOf(search.type) !== -1 &&
         search.operators.indexOf(search.operator) !== -1 &&
         search.name &&
         search.value;
    }
    function parseValue(value) {
      if(!isNaN(value)) {
        // value must be transformed to number
        return +value;
      }
      if(value === "true") {
        return true;
      }
      if(value === "false") {
        return false;
      }
      if(value === "NULL") {
        return null;
      }
      return value;
    }


    function updateQuery() {
      query.processVariables = [];
      query.taskVariables = [];
      query.caseInstanceVariables = [];
      angular.forEach($scope.searches, function(search) {
        if(isValid(search)) {
          query[typeTable[search.type]].push({
            name: search.name,
            operator: operatorTable[search.operator],
            value: parseValue(search.value)
          });
        }
      });
      tasklistData.set("taskListQuery", query);
    }

    var tasklistData = $scope.tasklistData.newChild($scope);
    var query;
    tasklistData.observe('taskListQuery', function(taskListQuery) {
      query = angular.copy(taskListQuery);
    });
  }];
});
