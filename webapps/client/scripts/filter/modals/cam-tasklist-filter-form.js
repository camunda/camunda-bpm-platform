define([
  'angular',
  'camunda-bpm-sdk',
  './cam-tasklist-filter-form-criteria',
  'text!./cam-tasklist-filter-form.html'
], function(
  angular,
  camSDK,
  criteria,
  template
) {
  'use strict';

  var each = angular.forEach;
  var copy = angular.copy;


  var criteriaExpressionSupport = {};
  var criteriaHelp              = {};
  var criteriaValidator         = {};
  each(criteria, function(group) {
    each(group.options, function(criterion) {
      criteriaExpressionSupport[criterion.name] = criterion.expressionSupport;
      criteriaHelp[criterion.name]              = criterion.help      || group.help;
      criteriaValidator[criterion.name]         = criterion.validate  || group.validate;
    });
  });

  var expressionsExp = /^[\s]*(\#|\$)\{/;
  function fixExpressions(arr) {
    each(arr, function(obj, i) {
      if (expressionsExp.test(obj.value) && obj.key.indexOf('Expression') === -1) {
        // arr[i].key = 'task'+ obj.key[0].toUpperCase() + obj.key.slice(1) +'Expression';
        arr[i].key = obj.key +'Expression';
      }
    });
    return arr;
  }

  var likeExp = /Like$/;
  function fixLikes(arr) {
    each(arr, function(obj, i) {
      if (likeExp.test(obj.key) && obj.value[0] !== '%') {
        arr[i].value = '%'+ obj.value +'%';
      }
    });
    return arr;
  }

  function unfixLikes(obj) {
    each(obj, function(val, key) {
      if (likeExp.test(key)) {
        if (val[0] === '%') {
          val = val.slice(1, val.length);
        }

        if (val.slice(-1) === '%') {
          val = val.slice(0, -1);
        }

        obj[key] = val;
      }
    });
    return obj;
  }

  function cleanJson(obj) {
    each(Object.keys(obj), function(key) {
      if (key === 'error' || key[0] === '$') {
        delete obj[key];
      }
      else if (angular.isObject(obj[key]) || angular.isArray(obj[key])) {
        obj[key] = cleanJson(obj[key]);
      }
    });
    return obj;
  }


  function unique(a) {
    return a.reduce(function(p, c) {
      if (p.indexOf(c) < 0) p.push(c);
      return p;
    }, []);
  }


  function makeObj(arr) {
    var obj = {};

    each(arr, function(item) {
      obj[item.key] = item.value;
    });

    return obj;
  }


  function makeArr(obj) {
    var arr = [];

    each(obj, function(value, key) {
      arr.push({
        key: key,
        value: value
      });
    });

    return arr;
  }



  function cleanArray(arr) {
    return unique(arr).map(function(a) { return (''+ a).trim(); });
  }



  function removeArrayItem(arr, delta) {
    var newArr = [];
    for (var key in arr) {
      if (key != delta) {
        newArr.push(arr[key]);
      }
    }
    return newArr;
  }


  var filterCreateModalCtrl = [
    '$location',
    '$scope',
    '$translate',
    'Notifications',
    'camAPI',
  function(
    $location,
    $scope,
    $translate,
    Notifications,
    camAPI
  ) {



    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Setup                                                                                        │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */
    var Filter = camAPI.resource('filter');
    var Authorization = camAPI.resource('authorization');



    $scope.$invalid = false;
    $scope.$valid = true;

    $scope.criteria = criteria;
    $scope.criteriaExpressionSupport = criteriaExpressionSupport;
    $scope.criteriaHelp = criteriaHelp;

    var state = $location.search();
    var opened = state.focus || 'general';
    $scope.accordion = {
      general:        opened === 'general',
      authorization:  opened === 'authorizations',
      query:          opened === 'query',
      variables:      opened === 'variables'
    };


    // dealing with hints that way,
    // allows to hide them if a translation is left empty (not undefined!)
    $scope.hints = {};
    $translate([
      'FILTER_FORM_BASICS_HINT',
      'FILTER_FORM_CRITERIA_HINT',
      'FILTER_FORM_AUTHORIZATIONS_HINT',
      'FILTER_FORM_VARIABLES_HINT'
    ])
    .then(function(result) {
      $scope.hints.general =        result.FILTER_FORM_BASICS_HINT;
      $scope.hints.criteria =       result.FILTER_FORM_CRITERIA_HINT;
      $scope.hints.authorizations = result.FILTER_FORM_AUTHORIZATIONS_HINT;
      $scope.hints.variables =      result.FILTER_FORM_VARIABLES_HINT;
    });


    $scope.filter =                       copy($scope.$parent.filter || {});
    $scope.filter.query =                 $scope.filter.query || [];
    $scope.filter.authorizations =        $scope.filter.authorizations || [];
    $scope.filter.properties =            $scope.filter.properties || {};
    $scope.filter.properties.variables =  $scope.filter.properties.variables || [];


    $scope.filter.properties.priority =   parseInt($scope.filter.properties.priority || 0, 10);
    $scope.filter.properties.color =      $scope.filter.properties.color || '#EEEEEE';





    function isValid() {
      $scope.$valid = !$scope._generalErrors.length &&
                      !$scope._variableErrors.length &&
                      !$scope._queryErrors.length &&
                      !$scope._authorizationErrors.length;

      $scope.$invalid = !$scope.$valid;

      return $scope.$valid;
    }



    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ General                                                                                      │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */
    $scope._generalErrors = [];
    var colorExp = /^#([0-9a-f]{6}|[0-9a-f]{3})$/i;
    $scope.validateGeneralInfo = function() {
      $scope._generalErrors = [];

      if (!$scope.filter.name) {
        $scope._generalErrors.push({field: 'name', message: 'REQUIRED_FIELD'});
      }

      if ($scope.filter.properties.priority % 1 !== 0) {
        $scope._generalErrors.push({field: 'priority', message: 'REQUIRED_INTEGER_FIELD'});
      }

      if (!colorExp.test($scope.filter.properties.color)) {
        $scope._generalErrors.push({field: 'color', message: 'REQUIRED_HEX_COLOR_FIELD'});
      }

      $scope._generalErrorsByField = {};
      each($scope._generalErrors, function(err) {
        $scope._generalErrorsByField[err.field] = err.message;
      });

      isValid();

      return $scope._generalErrors.length ? $scope._generalErrors : false;
    };



    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Ctriteria                                                                                    │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */

    var emptyCriterion = {
      key: '',
      operator: '',
      value: ''
    };

    $scope._query = [];
    var varExp = /Variables$/;
    each(makeArr(unfixLikes($scope.filter.query)), function(obj, o) {
      if (!varExp.test(obj.key)) {
        $scope._query.push(obj);
      }
    });

    $scope.addCriterion = function() {
      $scope._query.push(copy(emptyCriterion));
    };

    $scope.removeCriterion = function(delta) {
      $scope._query = removeArrayItem($scope._query, delta);
      $scope.validateCriteria();
    };

    $scope.simpleCriterionName = function(name) {
      if (!name) { return name; }
      // var simple = name.replace(/^task/, '').replace('Expression', '');
      var simple = name.replace('Expression', '');
      simple = simple[0].toLowerCase() + simple.slice(1);
      return simple;
    };

    $scope.validateCriterion = function(criterion, delta) {
      criterion.error = null;
      var name = $scope.simpleCriterionName(criterion.key);

      if (!name && criterion.value) {
        criterion.error = {field: 'key', message: 'REQUIRED_FIELD'};
      }
      else if (name && !criterion.value) {
        criterion.error = {field: 'value', message: 'REQUIRED_FIELD'};
      }
      else if (criteriaValidator[name]) {
        var validValue = criteriaValidator[name](criterion.value);
        console.info('validValue', validValue, criterion.value);
        if (validValue !== false) {
          criterion.error = {field: 'value', message: validValue || 'INVALID_FORMAT_FIELD'};
        }
      }

      return criterion.error;
    };

    $scope._queryErrors = [];
    $scope.validateCriteria = function() {
      $scope._queryErrors = [];

      each($scope._query, function(queryParam, delta) {
        var error = $scope.validateCriterion(queryParam, delta);
        if (error) {
          $scope._queryErrors.push(error);
        }
      });

      isValid();
      return $scope._queryErrors.length ? $scope._queryErrors : false;
    };





    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Authorizations                                                                               │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */

    var permissionsMap = ['ALL', 'READ', 'UPDATE', 'DELETE'];

    var emptyAuthorization = {
      type:                 1,
      identityType:         'user',
      identity:             '',
      permissions:          'ALL',
      availablePermissions: permissionsMap
    };

    var _authorizationsToDelete = [];

    $scope._authorizations = $scope.filter.authorizations;

    function availablePermissions(authorizationPermissions) {
      var available = copy(permissionsMap);

      each(authorizationPermissions, function(perm) {
        var i = available.indexOf(perm);
        if (i > -1) {
          available.splice(i, 1);
        }
      });

      if (available.length < 3) {
        available = ['ALL'];
      }

      return available;
    }

    each($scope._authorizations, function(authorization) {
      authorization.availablePermissions = availablePermissions(authorization.permissions);

      authorization.permissions = authorization.permissions.join(/[\s]*,[\s]*/);
      authorization.identity = authorization.userId || authorization.groupId;
      authorization.identityType = authorization.userId ? 'user' : 'group';
      authorization.type = parseInt(authorization.type, 10);
      authorization._originalType = authorization.type;
    });

    $scope.addAuthorization = function() {
      $scope._authorizations.push(copy(emptyAuthorization));
    };

    $scope.removeAuthorization = function(delta) {
      if ($scope._authorizations[delta].id) {
        $scope._authorizations[delta].toDelete = true;
      }
      else {
        $scope._authorizations = removeArrayItem($scope._authorizations, delta);
      }

      $scope.validateAuthorizations();
    };

    $scope.identityTypeSwitch = function(authorization, delta) {
      authorization.identityType = authorization.identityType === 'user' ? 'group' : 'user';
    };

    $scope.addPermission = function(authorization, permission) {
      if (permission === 'ALL') {
        authorization.permissions = 'ALL';
      }
      else if (authorization.permissions === 'ALL' || !authorization.permissions) {
        authorization.permissions = permission;
      }
      else {
        var arr = authorization.permissions.split(/[\s]*,[\s]*/);
        arr.push(permission);
        arr = cleanArray(arr);
        authorization.permissions = arr.join(', ');
      }

      authorization.availablePermissions = availablePermissions(authorization.permissions.split(/[\s]*,[\s]*/));
    };

    $scope.validateAuthorization = function(authorization, delta) {
      authorization.error = null;

      // ALLOW, DENY
      if (authorization.type !== '0') {
        if (!authorization.identity) {
          authorization.error = {field: 'identity', message: 'REQUIRED_FIELD'};
        }
      }
      // GLOBAL
      // else {

      // }

      return authorization.error;
    };

    $scope._authorizationErrors = [];
    $scope.validateAuthorizations = function() {
      $scope._authorizationErrors = [];

      each($scope._authorizations, function(authorization, delta) {
        var error = $scope.validateAuthorization(authorization, delta);
        if (error) {
          $scope._authorizationErrors.push(error);
        }
      });

      isValid();
      return $scope._authorizationErrors.length ? $scope._authorizationErrors : false;
    };






    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Variables                                                                                    │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */

    var emptyVariable = {
      name: '',
      label: ''
    };

    $scope._variables = $scope.filter.properties.variables;

    $scope.addVariable = function() {
      $scope._variables.push(copy(emptyVariable));
    };

    $scope.removeVariable = function(delta) {
      $scope._variables = removeArrayItem($scope._variables, delta);
      $scope.validateVariables();
    };

    $scope.validateVariable = function(variable, delta) {
      variable.error = null;

      if (!variable.name && variable.label) {
        variable.error = {field: 'name', message: 'REQUIRED_FIELD'};
      }
      else if (variable.name && !variable.label) {
        variable.error = {field: 'label', message: 'REQUIRED_FIELD'};
      }

      return variable.error;
    };

    $scope._variableErrors = [];
    $scope.validateVariables = function() {
      $scope._variableErrors = [];

      each($scope._variables, function(variable, delta) {
        var error = $scope.validateVariable(variable, delta);
        if (error) {
          $scope._variableErrors.push(error);
        }
      });

      isValid();
      return $scope._variableErrors.length ? $scope._variableErrors : false;
    };








    /*
    ┌──────────────────────────────────────────────────────────────────────────────────────────────┐
    │ Server ops                                                                                   │
    └──────────────────────────────────────────────────────────────────────────────────────────────┘
    */

    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : '')
        });
      });
    }

    function successNotification(src) {
      $translate(src).then(function(translated) {
        Notifications.addMessage({
          duration: 3000,
          status: translated
        });
      });
    }

    $scope.submit = function() {
      var toSave = {
        name:         $scope.filter.name,
        // owner:        $scope.filter.owner,
        resourceType: 'Task',
        query:        makeObj(fixExpressions(fixLikes($scope._query))),
        properties:   {
          color:        $scope.filter.properties.color,
          description:  $scope.filter.properties.description,
          priority:     $scope.filter.properties.priority,
          variables:    $scope._variables
        }
      };

      toSave = cleanJson(toSave);

      if ($scope.filter.id) {
        toSave.id = $scope.filter.id;
      }

      Filter.save(toSave, function(err, filterResponse) {
        if (err) {
          return errorNotification('FILTER_SAVE_ERROR', err);
        }

        var authTasks = [];
        each($scope._authorizations, function(auth) {
          auth = copy(auth);
          auth.type = parseInt(auth.type, 10);

          if (auth.type !== 0) {
            if (auth.identityType === 'user') {
              auth.userId = auth.identity;
            }
            else {
              auth.groupId = auth.identity;
            }
          }
          else {
            auth.groupId = null;
            auth.userId = '*';
          }

          auth.resourceId = toSave.id || filterResponse.id;
          auth.permissions = cleanArray(auth.permissions.split(','));
          auth.resourceType = 5;

          delete auth.identityType;
          delete auth.identity;
          delete auth.availablePermissions;
          auth = cleanJson(auth);


          var isNew = isNaN(auth._originalType);
          if (auth.toDelete) {
            authTasks.push(function(cb) { Authorization.delete(auth.id, cb); });
          }

          else if (isNew) {
            delete auth._originalType;
            authTasks.push(function(cb) { Authorization.create(auth, cb); });
          }

          else if (!isNew && auth.type != auth._originalType) {
            var newAuth = copy(auth);
            delete newAuth.id;
            delete newAuth._originalType;
            authTasks.push(function(cb) { Authorization.delete(auth.id, cb); });
            authTasks.push(function(cb) { Authorization.create(newAuth, cb); });
          }

          else {
            delete auth._originalType;
            delete auth.type;
            authTasks.push(function(cb) { Authorization.update(auth, cb); });
          }
        });

        camSDK.utils.series(authTasks, function(err, authTasksResult) {
          if (err) {
            return errorNotification('FILTER_AUTHORIZATION_SAVE_ERROR', err);
          }

          successNotification('FILTER_SAVE_SUCCESS');
          $scope.$emit('tasklist.filter.saved');
          $scope.$close();
        });
      });
    };



    $scope.deletion = typeof $scope.deletion === 'undefined' ? false : $scope.deletion;

    $scope.abortDeletion = function() {
      $scope.deletion = false;
    };

    $scope.confirmDeletion = function() {
      $scope.deletion = true;
    };

    $scope.delete = function() {
      Filter.delete($scope.filter.id, function(err) {
        if (err) {
          return errorNotification('FILTER_DELETION_ERROR', err);
        }

        successNotification('FILTER_DELETION_SUCCESS');
        $scope.$emit('tasklist.filter.deleted');
        $scope.$close();
      });
    };
  }];





  return [
    '$modal',
    '$scope',
    '$rootScope',
    '$location',
    '$translate',
    'Notifications',
    'camAPI',
  function(
    $modal,
    $scope,
    $rootScope,
    $location,
    $translate,
    Notifications,
    camAPI
  ) {
    var Filter = camAPI.resource('filter');
    var Authorization = camAPI.resource('authorization');
    $scope.loading = false;


    $rootScope.$watch('authentication.userCanCreateFilter', function() {
      if (!$rootScope.authentication) { return; }
      $scope.userCanCreateFilter = $rootScope.authentication.userCanCreateFilter;
    });



    function clearScopeFilter() {
      $scope.filter = null;
    }


    function open(filter) {
      if ($scope.filter && filter && $scope.filter.id === filter.id) {
        return;
      }

      // if not set to something truthy,
      // it could be possible to open a new modal window
      $scope.filter = filter || {};

      $modal.open({
        scope: $scope,

        windowClass: 'filter-edit-modal',

        size: 'lg',

        template: template,

        controller: filterCreateModalCtrl
      })
      .result.then(clearScopeFilter, clearScopeFilter);
    }


    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : '')
        });
      });
    }


    function loadPermissions(filter, loaded) {
      $scope.loading = true;
      Authorization.list({
        resourceType: 5,
        resourceId: filter.id
      }, function(err, authorizations) {
        $scope.loading = false;

        if (err) {
          return errorNotification('FILTER_AUTHORIZAION_NOT_FOUND', err);
        }

        filter.authorizations = authorizations;

        Filter.authorizations(filter.id, function(err, resp) {
          if (err) { throw err; }
          $scope.accesses = {};
          each(resp.links, function(link) {
            $scope.accesses[link.rel] = true;
          });

          loaded(filter);
        });
      });
    }


    function checkFilterState(evt, givenFilter, deletion) {
      $scope.deletion = deletion;
      if (givenFilter) {
        return loadPermissions(givenFilter, open);
      }

      var state = $location.search();
      if (state.filter) {

        if (state.filter !== true) {

          $scope.loading = true;
          return Filter.get(state.filter, function(err, filter) {
            $scope.loading = false;

            if (err) {
              return errorNotification('FILTER_NOT_FOUND', err);
            }

            loadPermissions(filter, open);
          });
        }

        // in case of a new filter
        open();
      }
    }


    $scope.$on('tasklist.filter.edit', checkFilterState);

    $scope.$on('tasklist.filter.delete', function(evt, givenFilter) {
      checkFilterState(evt, givenFilter, true);
    });

    checkFilterState();

    $scope.createFilter = function() {
      $scope.deletion = false;
      open();
    };
  }];
});
