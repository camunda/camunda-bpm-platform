define([
  'text!./cam-tasklist-filter-modal-form-permission.html'
], function(
  template
) {
  'use strict';

  var copy = angular.copy;
  var each = angular.forEach;

  var RESOURCE_TYPE = 5;

  return [
    'camAPI',
    '$q',
  function(
    camAPI,
    $q
  ) {

    return {

      restrict: 'A',
      require: '^camTasklistFilterModalForm',
      scope: {
        filter: '=',
        accesses: '=',
        filterModalFormData: '='
      },

      template: template,

      link: function ($scope, $element, attrs, parentCtrl) {

        // init //////////////////////////////////////////////////////////////////////////////

        var Authorization = camAPI.resource('authorization');

        var filterAuthorizationData = $scope.filterModalFormData.newChild($scope);

        var _form = $scope.filterPermissionForm;

        var authorizations = null;
        var globalAuthorization = null;
        var groupAuthorizationMap = null;
        var userAuthorizationMap = null;

        var NEW_DEFAULT_AUTHORIZATION = {
          resourceType: RESOURCE_TYPE,
          permissions: [ 'READ' ]
        };

        var NEW_PERMISSION = {
          type: 'user',
          id: null
        };

        var newPermission = $scope.newPermission = copy(NEW_PERMISSION);

        // register handler to show or hide the accordion hint /////////////////

        var showHintProvider = function () {
          var control = getNewPermissionField();

          return control && control.$error && control.$error.duplicate;
        };

        parentCtrl.registerHintProvider('filterPermissionForm', showHintProvider);

        // provide ////////////////////////////////////////////////////////////////////////

        filterAuthorizationData.provide('authorizations', ['filter', function (filter) {
          var deferred = $q.defer();

          if(!filter || !filter.id) {
            // no filter
            deferred.resolve([]);

          }
          else {

            Authorization.list({
              resourceType: RESOURCE_TYPE,
              resourceId: filter.id
            }, function (err, resp) {

              if(!!err) {
                deferred.reject(err);
              }
              else {
                deferred.resolve(resp);
              }

            });
          }

          return deferred.promise;

        }]);

        // observe ////////////////////////////////////////////////////////////////////////

        $scope.authorizationState = filterAuthorizationData.observe('authorizations', function(_authorizations) {
          authorizations = $scope.authorizations = copy(_authorizations) || [];
          initializeAuthorizations(authorizations);

          globalAuthorization = getGlobalAuthorization(authorizations);
          $scope.isGlobalReadAuthorization = hasReadPermission(globalAuthorization);

          groupAuthorizationMap = getAuthorziationMap(authorizations, 'groupId');
          userAuthorizationMap = getAuthorziationMap(authorizations, 'userId');
        });

        // handle global read permission ////////////////////////////////////////////////////

        $scope.globalReadAuthorizationChanged = function () {
          if ($scope.isGlobalReadAuthorization) {

            if (!globalAuthorization) {
              globalAuthorization = angular.extend({ userId: '*', type: 0 }, NEW_DEFAULT_AUTHORIZATION);
              authorizations.push(globalAuthorization);
            } else {
              addReadPermissionToAuthorization(globalAuthorization);
            }

            newPermission.id = null;
            validateNewPermission();
          }
          else {

            if (globalAuthorization) {
              removeReadPermissionFromAuthorization(globalAuthorization);
            }
          }
        };

        // handle new permission ////////////////////////////////////////////////////////////

        $scope.switchType = function ($event) {
          newPermission.type = newPermission.type === 'user' ? 'group' : 'user';
          validateNewPermission();
        };

        $scope.getReadAuthorizations = function (authorizations) {
          if (authorizations) {
            return getAuthorizationsWithReadPermissions(authorizations);
          }
        };

        var validateNewPermission = $scope.validateNewPermission = function () {
          var control = getNewPermissionField();

          control.$setValidity('authorization', true);
          control.$setValidity('duplicate', true);

          var id = newPermission.id;

          if (id) {
            // set the form invalid by setting an $error 'authorization'
            // so that the 'Save' button is disabled
            control.$setValidity('authorization', false);

            var auths = newPermission.type === 'user' ? userAuthorizationMap : groupAuthorizationMap;
            var auth = auths[id];

            if (auth && hasReadPermission(auth)) {
              return control.$setValidity('duplicate', false);
            }

          }
        };

        $scope.disableAddButton = function () {
          var control = getNewPermissionField();

          return $scope.isGlobalReadAuthorization || !newPermission.id || (control && control.$error && control.$error.duplicate);
        };

        var addReadPermission = $scope.addReadPermission = function () {
          var control = getNewPermissionField();

          var id = newPermission.id;

          var auths = newPermission.type === 'user' ? userAuthorizationMap : groupAuthorizationMap;
          var auth = auths[id];

          if (auth) {
            addReadPermissionToAuthorization(auth);

            var _authorizations = authorizations;
            authorizations = $scope.authorizations = [];

            for (var i = 0, _auth; !!(_auth = _authorizations[i]); i++) {
              if (_auth !== auth) {
                authorizations.push(_auth);
              }
            }

            authorizations.push(auth);
          }
          else {
            auth = { type : 1 };
            var prop = newPermission.type === 'user' ? 'userId' : 'groupId';
            auth[prop] = id;

            angular.extend(auth, NEW_DEFAULT_AUTHORIZATION);
            authorizations.push(auth);
            auths[id] = auth;
          }

          newPermission.id = null;

          control.$setValidity('authorization', true);
          control.$setPristine();
        };

        $scope.keyPressed = function ($event) {
          var keyCode = $event.keyCode;

          if (keyCode === 13) {

            if ($event.preventDefault) {
              // prevent executing switchType()
              $event.preventDefault();
            }

            var control = getNewPermissionField();

            return newPermission.id && control && (!control.$error || !control.$error.duplicate) && addReadPermission();
          }
        };

        // remove read permission ///////////////////////////////////////////////////////////

        $scope.removeReadPermission = function (auth) {
          removeReadPermissionFromAuthorization(auth);
          validateNewPermission();
        };

        // submit authorizations //////////////////////////////////////////////////////////

        var errors = [];

        var submitAuthorizations = function (filter, callback) {
          var actions = [];
          errors = [];

          if ($scope.isGlobalReadAuthorization) {

            for (var k = 0, auth; !!(auth = authorizations[k]); k++) {

              if (isGrantAuthorization(auth) && hasReadPermission(auth)) {
                // remove read permission so that the corresponding
                // authorizations will be updated or deleted
                removeReadPermissionFromAuthorization(auth);
              }
            }
          }

          for (var i = 0, authorization; !!(authorization = authorizations[i]); i++) {
            var permissions = authorization.permissions;
            var $permissions = authorization.$permissions;

            if (isGrantAuthorization(authorization) || isGlobalAuthorization(authorization)) {

              if (authorization.id) {

                // array of permissions is empty -> delete authorization
                if (!permissions.length && $permissions.length) {
                  // delete
                  actions.push({
                    type: 'delete',
                    authorization: authorization
                  });
                }
                else {

                  // permissions changed -> update authorization
                  if (permissions.length !== $permissions.length) {
                    // update
                    actions.push({
                      type: 'update',
                      authorization: authorization
                    });
                  }
                }
              }
              else {

                // authorization.id is null and at least one permission
                // has been added -> create authorization
                if (permissions.length) {
                  // create
                  actions.push({
                    type: 'create',
                    authorization: authorization
                  });
                }
              }
            }
          }

          performSubmit(actions, filter).then(function () {
            if (!errors || !errors.length) {
              errors = null;
            }

            if (typeof callback === 'function') {
              return callback(errors);
            }

          });

        };

        function performSubmit(actions, filter) {
          var deferred = $q.defer();

          actions = actions || [];
          var count = actions.length;

          function submitAction(type, authorization) {

            var $permissions = authorization.$permissions;

            delete authorization.$permissions;
            delete authorization.$$hashKey;

            authorization.resourceId = authorization.resourceId || filter.id;

            var callback = function (err, resp) {
              count = count - 1;

              if (!err) {

                if (type === 'create') {
                  authorization.id = resp.id;
                  authorization.permissions = copy(resp.permissions ||[]);
                  authorization.$permissions = copy(resp.permissions || []);
                }
                else {
                  if (type === 'delete') {
                    authorization.id = null;
                  }

                  authorization.permissions = copy(authorization.permissions || []);
                  authorization.$permissions = copy(authorization.permissions || []);
                }
              }
              else {
                errors.push({
                  status: 'FILTER_FORM_PERMISSIONS_SAVE_ERROR',
                  error: err
                });

                // set $permissions again
                authorization.$permissions = $permissions;
              }

              if (count === 0) {
                deferred.resolve();
              }

            };

            if (type === 'create') {
              Authorization.create(authorization, callback);
            }
            else if (type === 'update') {
              Authorization.update(authorization, callback);
            }
            else if (type === 'delete') {
              Authorization.delete(authorization.id, callback);
            }
          }

          if (count === 0) {
            deferred.resolve();
          }

          for (var i = 0, action; !!(action = actions[i]); i++) {
            var type = action.type;
            // do not create a copy of authorization, if there is a
            // failure during submitting the authorizations, the dialog
            // stays open and the user could try to save the dialog once
            // again
            var authorization = action.authorization;

            submitAction(type, authorization);
          }

          return deferred.promise;
        }

        parentCtrl.registerPostFilterSavedProvider(submitAuthorizations);

        // helper /////////////////////////////////////////////////////////////////////////

        function initializeAuthorizations (authorizations) {
          for (var i = 0, authorization; !!(authorization = authorizations[i]); i++) {
            // save the original permissions
            authorization.$permissions = copy(authorization.permissions || []);
          }
        }

        function getNewPermissionField () {
          return _form.newPermission;
        }

        function isGlobalAuthorization (authorization) {
          return authorization && authorization.type === 0;
        }

        function isGrantAuthorization (authorization) {
          return authorization && authorization.type === 1;
        }

        function isGlobalUserOrGroupId (authorization) {
          authorization = authorization || {};
          var id = authorization.userId || authorization.groupId;
          return id === '*';
        }

        function hasProperty (authorization, prop) {
          return !!authorization[prop];
        }

        function hasReadPermission(authorization) {
          if (authorization && authorization.permissions) {
            var permissions = authorization.permissions;
            for (var i = 0, perm; !!(perm = permissions[i]); i++) {
              if (perm === 'READ' || perm === 'ALL') {
                return true;
              }
            }
          }
          return false;
        }

        function getGlobalAuthorization(authorizations) {
          for (var i = 0, authorization; !!(authorization = authorizations[i]); i++) {
            if (isGlobalAuthorization(authorization)) {
              return authorization;
            }
          }
        }

        function getAuthorizationsWithReadPermissions(authorizations) {
          var result = [];

          for (var i = 0, authorization; !!(authorization = authorizations[i]); i++) {

            if (isGrantAuthorization(authorization)) {

              if (!isGlobalUserOrGroupId(authorization) && hasReadPermission(authorization)) {
                result.push(authorization);
              }

            }
          }

          return result;
        }

        function getAuthorziationMap(authorizations, criteria) {
          var _authorizations = getAuthorziations(authorizations, criteria);
          var obj = {};

          for (var i = 0, authorization; !!(authorization = _authorizations[i]); i++) {
            var _criteria = authorization[criteria];
            obj[_criteria] = authorization;
          }

          return obj;
        }

        function getAuthorziations(authorizations, criteria) {
          var result = [];

          for (var i = 0, authorization; !!(authorization = authorizations[i]); i++) {

            if (isGrantAuthorization(authorization)) {

              if (hasProperty(authorization, criteria) && !isGlobalUserOrGroupId(authorization)) {
                result.push(authorization);
              }

            }

          }

          return result;
        }

        function addReadPermissionToAuthorization(authorization) {
          if (authorization) {
            var permissions = authorization.permissions;

            if (!permissions || !permissions.length) {
              authorization.permissions = [ 'READ' ];
            }

            else if (permissions && permissions.length === 1) {
              var permission = permissions[0];
              authorization.permissions = authorization.permissions.concat([ 'READ' ]);
            }

            else {
              authorization.permissions = [ 'ALL' ];
            }
          }
        }

        function removeReadPermissionFromAuthorization(authorization) {
          if (authorization) {
            var permissions = authorization.permissions;

            if (permissions && permissions.length === 1) {
              var permission = permissions[0];

              if (permission === 'ALL') {
                authorization.permissions = [ 'UPDATE', 'DELETE' ];
              }
              else if (permission === 'READ') {
                authorization.permissions = [];
              }
            }
            else {

              authorization.permissions = [];

              for (var i = 0, perm; !!(perm = permissions[i]); i++) {
                if (perm !== 'READ') {
                  authorization.permissions.push(perm);
                }
              }

            }
          }
        }

      }
    };

  }];

});
