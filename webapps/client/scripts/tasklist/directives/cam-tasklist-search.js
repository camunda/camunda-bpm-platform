define([
  'angular',
  'text!./cam-tasklist-search.html',
  'text!./cam-tasklist-search-config.json'
], function(
  angular,
  template,
  searchConfigJSON
) {
  'use strict';

  var searchConfig = JSON.parse(searchConfigJSON);

  function getOperatorObjectFromString(operatorString) {
    switch(operatorString) {
      case '!=': return {key: 'neq', value: operatorString};
      case '<=': return {key: 'lteq', value: operatorString};
      case '>=': return {key: 'gteq', value: operatorString};
      case 'like': return {key: 'like', value: operatorString};
      case '=': return {key: 'eq', value: operatorString};
      case '<': return {key: 'lt', value: operatorString};
      case '>': return {key: 'gt', value: operatorString};
    }
  }

  function getTypeObjectFromString(typeString) {
    switch(typeString) {
      case 'Process Variable': return searchConfig.types[0];
      case 'Task Variable': return searchConfig.types[1];
      case 'Case Variable': return searchConfig.types[2];

    }
  }

  function parseSearch(search, query) {
    var searchRegEx = /^\s*(.*?)\s*(!=|<=|>=|like|[=<>])\s*(.*?)\s*$/;

    var match = searchRegEx.exec(query);
    if(match && match.length === 4) {
      search.name.value = match[1].trim();
      search.value.value = match[3];
      search.operator.value = getOperatorObjectFromString(match[2]);
      search.operator.values = getOperators(getType(parseValue(search.value.value)));
      return true;
    }
    return false;
  }

  function getOperators(varType) {
    return searchConfig.operators[varType];
  }

  var dateRegex = /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/;
  function getType(value) {
    if(value && typeof value === 'string' && value.match(dateRegex)) {
      return 'date';
    }
    return typeof value;
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
    '$translate',
    'search',
  function(
    $timeout,
    $location,
    $translate,
    search
  ) {

    function createSearchObj(type) {
      var translated = $translate.instant(['VARIABLE_TYPE', 'PROPERTY', 'OPERATOR', 'VALUE']);
      return {
        type : {value: getTypeObjectFromString(type), values: searchConfig.types, tooltip: translated.VARIABLE_TYPE},
        name : {tooltip: translated.PROPERTY},
        operator : {values: searchConfig.operators, tooltip: translated.OPERATOR},
        value : {tooltip: translated.VALUE}
      };
    }

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

        $scope.dropdownOpen = false;
        $scope.types = searchConfig.types.map(function(el) {
          return el.value;
        });

        $scope.searches = [];

        $scope.deleteSearch = function(idx) {
          var needsUpdate = $scope.isValid($scope.searches[idx]);
          $scope.searches.splice(idx,1);
          if(needsUpdate) {
            updateQuery();
          }
        };

        $scope.createSearch = function(type){
          var search = createSearchObj(type);
          if(!parseSearch(search, $scope.inputQuery)) {
            search.value.value = $scope.inputQuery;
            search.operator.values = getOperators(getType(parseValue(search.value)));
          }
          $scope.searches.push(search);
          if($scope.isValid(search)) {
            updateQuery();
          }

          // need to use timeout, because jQuery initiates an apply cycle
          // while the current apply cycle is still in progress
          $timeout(function(){angular.element('.search-container > input').blur();});
          $scope.dropdownOpen = false;
          $scope.inputQuery = '';
        };

        $scope.changeSearch = function(idx, field, before, value) {

          var search = $scope.searches[idx];

          // temporarily restore the old state to check if the field needs an update
          search[field].value = before;
          var needsUpdate = $scope.isValid(search);

          if(field === 'name') {
            // trim the variable name for the model (needed for the *validation*)
            search[field].value = value.trim();

            // trim the name again AFTER the inline field widget overwrites it with the
            // entered value (which may contain whitespace) - needed for *style*
            $timeout(function(){
              search[field].value = value.trim();
            });
          } else {
            search[field].value = value;
          }

          var valueType = getType(parseValue(search.value.value));
          search.operator.values = getOperators(valueType);
          if(search.operator.value && search.operator.values.map(function(el){
            return el.key;
          }).indexOf(search.operator.value.key) === -1) {
            search.operator.value = search.operator.values[0];
          }
          if(needsUpdate || $scope.isValid(search)) {
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

        $scope.isValid = function(search) {
          return !!search.type.value &&
             !!search.operator.value &&
             !!search.name.value &&
             !!search.value.value;
        };

        var searchData = $scope.tasklistData.newChild($scope);

        function updateQuery() {
          var outArray = [];
          angular.forEach($scope.searches, function(search) {
            if($scope.isValid(search)) {
              outArray.unshift({
                name: search.name.value,
                operator: search.operator.value.key,
                value: parseValue(search.value.value),
                type: search.type.value.key
              });
            }
          });

          search.updateSilently({
            query: JSON.stringify(outArray)
          });

          searchData.changed('taskListQuery');
        }

        function createSearchFromURL(urlSearch) {
          var search = createSearchObj();
          search.type.value = searchConfig.types.reduce(function(done, el) {
              return done || (el.key === urlSearch.type ? el : null);
            }, null);
          search.name.value = urlSearch.name;
          search.value.value = urlSearch.value === null ? "NULL" : urlSearch.value.toString();
          search.operator.value = getOperators(getType(search.value.value)).reduce(function(done, el) {
              return done || (el.key === urlSearch.operator ? el : null);
            }, null);
          search.operator.values = getOperators(getType(search.value.value));
          return search;
        }

         searchData.observe('taskListQuery', function(taskListQuery) {

           var search, i;
           for(i = 0; i < $scope.searches.length; i++) {
             search = $scope.searches[i];
             if($scope.isValid(search)) {
                $scope.searches.splice(i, 1);
                i--;
             }
           }

           var searches = JSON.parse(getPropertyFromLocation('query'));

           if(searches) {
             for(i=0; i < searches.length; i++) {
               $scope.searches.unshift(createSearchFromURL(searches[i]));
             }
           }

         });

      },

      template: template
    };
  }];
});
