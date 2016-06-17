'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/authorizations.html', 'utf8');
var confirmTemplate = fs.readFileSync(__dirname + '/confirm-delete-authorization.html', 'utf8');

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/authorization', {
    template: template,
    controller: [
      '$scope', 'page',      '$routeParams', '$modal', 'AuthorizationResource', 'Notifications', '$location',
      function($scope,   pageService, $routeParams,   $modal,   AuthorizationResource,   Notifications,   $location) {

        $scope.$root.showBreadcrumbs = true;

        pageService.titleSet('Authorizations');

        pageService.breadcrumbsClear();

        $scope.allPermissionsValue = 2147483647;

        $scope.resourceMap = {
          0: 'Application',
          1: 'User',
          2: 'Group',
          3: 'Group Membership',
          4: 'Authorization',
          5: 'Filter',
          6: 'Process Definition',
          7: 'Task',
          8: 'Process Instance',
          9: 'Deployment',
          10: 'Decision Definition',
          11: 'Tenant',
          12: 'Tenant Membership',
          13: 'Batch'
        };

        pageService.breadcrumbsAdd([
          {
            label: 'Authorizations',
            href: '#/authorization'
          }
        ]);

        $scope.permissionMap = {
          0: [ 'ACCESS' ],
          1: [ 'READ', 'UPDATE', 'CREATE', 'DELETE' ],
          2: [ 'READ', 'UPDATE', 'CREATE', 'DELETE' ],
          3: [ 'CREATE', 'DELETE' ],
          4: [ 'READ', 'UPDATE', 'CREATE', 'DELETE' ],
          5: [ 'READ', 'UPDATE', 'DELETE' ],
          6: [ 'READ', 'CREATE_INSTANCE', 'READ_INSTANCE', 'UPDATE_INSTANCE', 'DELETE_INSTANCE', 'MIGRATE_INSTANCE', 'READ_TASK', 'UPDATE_TASK', 'TASK_ASSIGN', 'TASK_WORK', 'READ_HISTORY', 'DELETE_HISTORY' ],
          7: [ 'CREATE', 'READ', 'UPDATE', 'DELETE', 'TASK_ASSIGN', 'TASK_WORK' ],
          8: [ 'CREATE', 'READ', 'UPDATE', 'DELETE' ],
          9: [ 'CREATE', 'READ', 'DELETE' ],
          10: [ 'READ', 'CREATE_INSTANCE', 'READ_HISTORY', 'DELETE_HISTORY' ],
          11: [ 'READ', 'UPDATE', 'CREATE', 'DELETE' ],
          12: [ 'CREATE', 'DELETE' ],
          13: [ 'READ', 'UPDATE', 'CREATE', 'DELETE', 'READ_HISTORY', 'DELETE_HISTORY' ]
        };

        $scope.typeMap = {
          0: 'GLOBAL',
          1: 'ALLOW',
          2: 'DENY'
        };

        $scope.getIdentityId = function(auth) {
          if(auth.userId) {
            return auth.userId;
          } else {
            return auth.groupId;
          }
        };

        function createResourceList() {
          $scope.resourceList = [];
          for(var entry in $scope.resourceMap) {
            $scope.resourceList.push({id:entry, name:$scope.resourceMap[entry]});
          }
        }

        var getType = $scope.getType = function(authorization) {
          return $scope.typeMap[authorization.type];
        };

        var getResource = $scope.getResource = function(resourceType) {
          return $scope.resourceMap[resourceType];
        };

        var formatPermissions = $scope.formatPermissions = function(permissionsList) {

          // custom handling of NONE:
          // (permission NONE is trivially contained in all GRANTs and GLOBALs)
          var nonePos = permissionsList.indexOf('NONE');
          if(nonePos > -1) {
            permissionsList = permissionsList.splice(nonePos,1);
          }

          // remove others if ALL is contained:
          if(permissionsList.indexOf('ALL')>-1) {
            return 'ALL';

          } else {
            var result = '';
            for (var i = 0; i < permissionsList.length; i++) {
              if(i>0) {
                result += ', ';
              }
              result += permissionsList[i];
            }
            return result;
          }
        };

        $scope.deleteAuthorization = function(authorization) {

          var dialog = $modal.open({
            controller: 'ConfirmDeleteAuthorizationController',
            template: confirmTemplate,
            resolve: {
              authorizationToDelete: function() { return authorization; },
              formatPermissions: function() { return formatPermissions; },
              getResource: function() { return getResource; },
              getType: function() { return getType; }
            }
          });

          dialog.result.then(function(result) {
            if (result == 'SUCCESS') {
              loadAuthorizations();
            }
          }, function() {
            loadAuthorizations();
          });
        };


        $scope.pages = $scope.pages || {
          total: 0,
          size: 25,
          current: 1
        };

        var loadAuthorizations = $scope.loadAuthorizations = function() {
          $scope.loadingState = 'LOADING';
          function reqError() {
            $scope.loadingState = 'ERROR';
          }

          AuthorizationResource.count({
            resourceType :  $scope.selectedResourceType
          }).$promise.then(function(response) {
            $scope.pages.total = response.count;
          }, reqError);

          AuthorizationResource.query({
            resourceType :  $scope.selectedResourceType,
            firstResult:    ($scope.pages.current - 1) * $scope.pages.size,
            maxResults:     $scope.pages.size
          }).$promise.then(function(response) {
            $scope.authorizations = response;
            $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
          }, reqError);

        };

        // will also trigger the initial load request
        $scope.$watch('pages.current', loadAuthorizations);

        $scope.getPermissionsForResource = function() {
          if($scope.selectedResourceType) {
            return $scope.permissionMap[$scope.selectedResourceType];
          } else {
            return [];
          }
        };

        // page controls ////////////////////////////////////

        $scope.show = function(fragment) {
          return fragment == $location.search().tab;
        };

        $scope.activeClass = function(link) {
          var path = $location.absUrl().split('?').pop();
          return path === link ? 'active' : '';
        };

        // init ////////////////////////////////////

        createResourceList();

        if(!$location.search().resource) {
          $location.search({'resource': 0});
          $location.replace();
          $scope.title = $scope.getResource(0);
          $scope.selectedResourceType = '0';

        } else {
          $scope.title = $scope.getResource($routeParams.resource);
          $scope.selectedResourceType = $routeParams.resource;
        }
      }],
    authentication: 'required',
    reloadOnSearch: false
  });
}];
