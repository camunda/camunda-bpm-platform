'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

var copy = angular.copy;
var each = angular.forEach;
var isArray = angular.isArray;
var isObject = angular.isObject;

var RESOURCE_TYPE = 'Task';
var DEFAULT_COLOR = '#555555';

var likeExp = /Like$/;
function fixLike(key, value) {
  if (likeExp.test(key)) {

    if (value[0] !== '%') {
      value = '%' + value;
    }

    var length = value.length - 1;
    if (value[length] !== '%') {
      value = value + '%';
    }

  }
  return value;
}

function unfixLike(key, value) {
  if (likeExp.test(key)) {
    if (value[0] === '%') {
      value = value.slice(1, value.length);
    }

    if (value.slice(-1) === '%') {
      value = value.slice(0, -1);
    }
  }
  return value;
}

var varExp = /Variables$/;
function isQueryVariable(key) {
  return varExp.test(key);
}

var expressionsExp = /^[\s]*(\#|\$)\{/;
function isExpression(value) {
  return expressionsExp.test(value);
}

function cleanJson(obj) {
  each(Object.keys(obj), function(key) {
      // property with name starting with "$" or empty arrays are removed
    if (key[0] === '$' || (isArray(obj[key]) && !obj[key].length)) {
      delete obj[key];
    }
    else if (isObject(obj[key]) || isArray(obj[key])) {
      obj[key] = cleanJson(obj[key]);
    }
  });
  return obj;
}

module.exports = [
  '$scope',
  '$translate',
  '$q',
  'Notifications',
  'camAPI',
  'filter',
  'filtersData',
  function(
    $scope,
    $translate,
    $q,
    Notifications,
    camAPI,
    filter,
    filtersData
  ) {

    var Filter = camAPI.resource('filter');

    var filterModalData = $scope.filterModalData = filtersData.newChild($scope);

    $scope.$on('$locationChangeStart', function() {
      $scope.$dismiss();
    });

    $scope.deletion = false;

    // init ////////////////////////////////////////////////////////////////////////

    // initialize filter
    $scope.filter =                        copy(filter || {});

    // initialize filter name
    $scope.filter.name =                   $scope.filter.name;

    // initialize filter properties
    $scope.filter.properties =             $scope.filter.properties || {};

    $scope.filter.properties.description = $scope.filter.properties.description;
    $scope.filter.properties.priority =    parseInt($scope.filter.properties.priority || 0, 10);
    $scope.filter.properties.color =       $scope.filter.properties.color || DEFAULT_COLOR;
    $scope.filter.properties.refresh =     $scope.filter.properties.refresh || false;
    $scope.filter.properties.showUndefinedVariable = $scope.filter.properties.showUndefinedVariable || false;

    var filterId =                         $scope.filter.id;

    // initialize variables
    $scope.filter.properties.variables =   $scope.filter.properties.variables || [];

    // initialize filter query
    var _query = $scope.filter.query =     $scope.filter.query || {};

     // transform filter query object into an array
    var query = [];
    var queryVariables = [];

    for (var key in _query) {
      var value = _query[key];

      if (!isQueryVariable(key)) {
        query.push({
          key: key,
          value: unfixLike(key, value)
        });
      }
      else {
        queryVariables.push({
          key: key,
          value: value
        });
      }
    }

    $scope.filter.query = query;

    // provide data ///////////////////////////////////////////////////////////////

    filterModalData.provide('filter', $scope.filter);

    filterModalData.provide('userFilterAccess', ['filter', function(filter) {
      var deferred = $q.defer();

      if(!filter || !filter.id) {
        // no filter
        deferred.resolve({
          links: []
        });

      }
      else {
        Filter.authorizations(filter.id, function(err, resp) {

          if(err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(resp);
          }

        });
      }

      return deferred.promise;

    }]);

    filterModalData.provide('accesses', ['userFilterAccess', function(access) {
      var accesses = {};
      each(access.links, function(link) {
        accesses[link.rel] = true;
      });
      return accesses;
    }]);

    // observe date //////////////////////////////////////////////////////////

    filterModalData.observe('accesses', function(accesses) {
      $scope.accesses = accesses;
    });

    // provider ////////////////////////////////////

    var defaultValidationProvider = function() {
      return false;
    };

    $scope.isValid = defaultValidationProvider;

    $scope.registerValidationProvider = function(fn) {
      $scope.isValid = fn || defaultValidationProvider;
    };

    var postFilterSavedProvider = function(filter, callback) {
      return callback();
    };

    $scope.registerPostFilterSavedProvider = function(fn) {
      postFilterSavedProvider = fn || postFilterSavedProvider;
    };

    // submit /////////////////////////////////////

    function errorNotification(src, err, exclusive) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : ''),
          exclusive: exclusive,
          scope: $scope
        });
      });
    }

    $scope.submit = function() {
      $scope.$broadcast('pre-submit');

      // transform query array into a query object
      var _queryArray = ($scope.filter.query || []).concat(queryVariables);
      var _queryObj = {};

      for (var i = 0, elem; (elem = _queryArray[i]); i++) {

        var key = elem.key;
        var value = elem.value;

        if (!isQueryVariable(key)) {

          // if key == '...Like' -> value = '%' + value + '%'
          value = fixLike(key, value);

          if (isExpression(value)) {

            if(key.indexOf('Expression') === -1) {
              key = key +'Expression';
            }

          } else {
            if(key.indexOf('Expression') !== -1) {
              key = key.slice(0, key.indexOf('Expression'));
            }
          }

          // for "in" criterion, the values are passed as coma separated list
          if ((key === 'candidateGroups' || key.slice(-2) === 'In')) {
            if( typeof value === 'string') {
              value = value.split(',');
              for (var v = 0; v < value.length; v++) {
                if (value[v]) {
                  value[v] = value[v].trim();
                }
              }
            }
          } else {
            value = ''+value;
          }
        }

        _queryObj[key] = value;
      }

      if ($scope.filter.includeAssignedTasks) {
        _queryObj.includeAssignedTasks = true;
      }

      var toSave = {
        id:           filterId,
        name:         $scope.filter.name,
        resourceType: RESOURCE_TYPE,
        query:        _queryObj,
        properties:   {
          description:  $scope.filter.properties.description,
          priority:     parseInt($scope.filter.properties.priority || 0, 10),
          color:        $scope.filter.properties.color || DEFAULT_COLOR,
          refresh:      $scope.filter.properties.refresh,
          variables:    $scope.filter.properties.variables,
          showUndefinedVariable: $scope.filter.properties.showUndefinedVariable
        }
      };

      cleanJson(toSave);

      Filter.save(toSave, function(err, filterResponse) {
        if (err) {
          return errorNotification('FILTER_SAVE_ERROR', err, true);
        }

        toSave.id = filterId = filterId || filterResponse.id;

        postFilterSavedProvider(toSave, function(err) {

          if (err) {
            if (isArray(err) && err.length) {
              for (var i = 0, error; (error = err[i]); i++) {
                errorNotification(error.status, error.error, i === 0);
              }
            }
            else {
              errorNotification(err.status, err.error, true);
            }
            return;
          }

          $scope.$close();

        });

      });
    };

    // deletion ////////////////////////////////////////////////////////////////////

    $scope.abortDeletion = function() {
      $scope.deletion = false;
    };

    $scope.confirmDeletion = function() {
      $scope.deletion = true;
    };

    $scope.delete = function() {
      Filter.delete($scope.filter.id, function(err) {
        if (err) {
          return errorNotification('FILTER_DELETION_ERROR', err, true);
        }

        $scope.$close();

      });
    };

  }];
