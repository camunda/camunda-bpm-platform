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
        return true;
      }
      return false;
    }
    function createSearchObj(type) {
      return {
        type : type,
        operator: "="
      };
    }

    $scope.types = ["Process Variable", "Task Variable", "Case Variable"];
    $scope.operatorList = ["=", "!=", "<", ">", "<=", ">=", "like"];
    $scope.dropdownOpen = false;

    $scope.deleteSearch = function(idx) {
      $scope.searches.splice(idx,1);
      updateQuery();
    };

    $scope.createSearch = function(type){
      var search = createSearchObj(type);
      if(!parseSearch(search, $scope.inputQuery)) {
        search.value = $scope.inputQuery;
      }
      $scope.searches.push(search);
      updateQuery();

      // need to use timeout, because jQuery initiates an apply cycle
      // while the current apply cycle is still in progress
      $timeout(function(){angular.element('.search-container > input').blur();});
      $scope.dropdownOpen = false;
      $scope.inputQuery = "";
    };

    $scope.changeSearch = function(idx, field, value) {
      $scope.searches[idx][field] = value;
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
         $scope.operatorList.indexOf(search.operator) !== -1 &&
         search.name &&
         search.value;
    }
    function parseValue(value) {
      // cast input value to number if needed
      return isNaN(value) ? value : +value;
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
