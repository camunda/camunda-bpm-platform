define([
  'angular',
  'text!./cam-tasklist-search.html'
], function(
  angular,
  template
) {
  'use strict';

  function parseSearch(search, query) {
    var searchRegEx = /^\s*(.*?)\s*(!=|<=|>=|like|[=<>])\s*(.*?)\s*$/;

    var match = searchRegEx.exec(query);
    if(match && match.length === 4) {
      search.name = match[1].trim();
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
      operator: '=',
      operators: getOperators()
    };
  }

  function getOperators(varType) {
    switch(varType) {
      case 'date':    return ['BEFORE', 'AFTER'];
      case 'boolean':
      case 'object':  return ['=', '!='];
      case 'number':  return ['=', '!=', '<', '>', '<=', '>='];
      default:        return ['=', '!=', '<', '>', '<=', '>=', 'like'];
    }
  }

  var dateRegex = /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/;
  function getType(value) {
    if(value && typeof value === 'string' && value.match(dateRegex)) {
      return 'date';
    }
    return typeof value;
  }

  function getDefaultOperator(valueType) {
    switch(valueType) {
      case 'date': return 'AFTER';
      default:     return '=';
    }
  }

  function parseValue(value) {
    if(!isNaN(value) && value.trim() !== '') {
      // value must be transformed to number
      return +value;
    }
    if(value === 'true') {
      return true;
    }
    if(value === 'false') {
      return false;
    }
    if(value === 'NULL') {
      return null;
    }
    return value;
  }

  return [
    '$timeout',
    '$location',
    'search',
  function(
    $timeout,
    $location,
    search
  ) {

    return {
      restrict: 'A',

      scope: {
        tasklistData: '='
      },

      link: function($scope, element, attrs) {

        function getPropertyFromLocation(property) {
          var search = $location.search() || {};
          return search[property] || null;
         }

        $scope.types = ['Process Variable', 'Task Variable', 'Case Variable'];
        $scope.dropdownOpen = false;

        $scope.searches = [];

        $scope.invalidSearch = function(search) {
          return !isValid(search);
        };

        $scope.deleteSearch = function(idx) {
          var needsUpdate = isValid($scope.searches[idx]);
          $scope.searches.splice(idx,1);
          if(needsUpdate) {
            updateQuery();
          }
        };

        $scope.createSearch = function(type){
          var search = createSearchObj(type);
          if(!parseSearch(search, $scope.inputQuery)) {
            search.value = $scope.inputQuery;
            search.operators = getOperators(getType(parseValue(search.value)));
          }
          $scope.searches.push(search);
          if(isValid(search)) {
            updateQuery();
          }

          // need to use timeout, because jQuery initiates an apply cycle
          // while the current apply cycle is still in progress
          $timeout(function(){angular.element('.search-container > input').blur();});
          $scope.dropdownOpen = false;
          $scope.inputQuery = '';
        };

        $scope.changeSearch = function(idx, field, value) {
          var search = $scope.searches[idx];
          var needsUpdate = isValid(search);
          if(field === 'name') {
            // trim the variable name for the model (needed for the *validation*)
            search[field] = value.trim();

            // trim the name again AFTER the inline field widget overwrites it with the
            // entered value (which may contain whitespace) - needed for *style*
            $timeout(function(){search[field] = value.trim();});
          } else {
            search[field] = value;
          }

          var valueType = getType(parseValue(search.value));
          search.operators = getOperators(valueType);
          if(search.operators.indexOf(search.operator) === -1) {
            // if the current value type does not allow the selected operator,
            // fall back to default operator
            search.operator = getDefaultOperator(valueType);
          }
          if(needsUpdate || isValid(search)) {
            updateQuery();
          }
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

        function isValid(search) {
          return $scope.types.indexOf(search.type) !== -1 &&
             search.operators.indexOf(search.operator) !== -1 &&
             !!search.name &&
             !!search.value;
        }

        var searchData = $scope.tasklistData.newChild($scope);

        function updateQuery() {
          var outArray = [];
          angular.forEach($scope.searches, function(search) {
            if(isValid(search)) {
              outArray.unshift({
                name: search.name,
                operator: search.operator,
                value: parseValue(search.value),
                type: search.type
              });
            }
          });

          search.updateSilently({
            query: JSON.stringify(outArray)
          });

          searchData.changed('taskListQuery');
        }

         searchData.observe('taskListQuery', function(taskListQuery) {

           var search, i;
           for(i = 0; i < $scope.searches.length; i++) {
             search = $scope.searches[i];
             if(isValid(search)) {
                $scope.searches.splice(i, 1);
                i--;
             }
           }

           var searches = JSON.parse(getPropertyFromLocation('query'));

           if(searches) {
             for(i=0; i < searches.length; i++) {
               search = searches[i];
               search.operators = getOperators(getType(search.value));
               if(search.value === null) {
                 search.value = "NULL";
               } else {
                 search.value = search.value.toString();
               }
               search.type = search.type;
               $scope.searches.unshift(search);
             }
           }

         });

      },

      template: template
    };
  }];
});
