define([ 'angular', 'require' ], function(angular, require) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', '$dialog', 'AuthorizationResource', 'Notifications', '$location', 
    function ($scope, $routeParams, $dialog, AuthorizationResource, Notifications, $location) {

    $scope.allPermissionsValue = 2147483647;

    $scope.resourceMap = {
      0: "Application",
      1: "User",
      2: "Group",
      3: "Group Membership",
      4: "Authorization"
    };

    $scope.permissionMap = {
      0: [ "ACCESS" ],
      1: [ "READ", "UPDATE", "CREATE", "DELETE" ],
      2: [ "READ", "UPDATE", "CREATE", "DELETE" ],
      3: [ "CREATE", "DELETE" ],
      4: [ "READ", "UPDATE", "CREATE", "DELETE" ],
    }

    $scope.typeMap = {
      0: "GLOBAL",
      1: "ALLOW",
      2: "DENY"      
    };

    $scope.getIdentityId = function(auth) {
      if(!!auth.userId) {
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
    }
    
    var getResource = $scope.getResource = function(resourceType) {      
      return $scope.resourceMap[resourceType];
    }

    var formatPermissions = $scope.formatPermissions = function(permissionsList) {    
        
      // custom handling of NONE:
      // (permission NONE is trivially contained in all GRANTs and GLOBALs)
      var nonePos = permissionsList.indexOf("NONE");
      if(nonePos > -1) {
        permissionsList = permissionsList.splice(nonePos,1);
      }

      // remove others if ALL is contained:
      if(permissionsList.indexOf("ALL")>-1) {
        return "ALL";

      } else {
        var result = "";
        for (var i = 0; i < permissionsList.length; i++) {
          if(i>0) {
            result += ", ";
          } 
          result += permissionsList[i];
        };
        return result;
      }
    };

    $scope.deleteAuthorization = function(authorization) {
      
      var dialog = $dialog.dialog({
        controller: 'ConfirmDeleteAuthorizationController',
        templateUrl: require.toUrl('./confirm-delete-authorization.html'),
        resolve: {
          authorizationToDelete: function() { return authorization; },
          formatPermissions: function() { return formatPermissions; },
          getResource: function() { return getResource; },
          getType: function() { return getType; }
        }
      });

      dialog.open().then(function(result) {

        if (result == 'SUCCESS') {
          loadAuthorizations();
        }
      });   
    };

    var loadAuthorizations = $scope.loadAuthorizations = function() {
      AuthorizationResource.query({resourceType : $scope.selectedResourceType}).$then(function(response) {
        $scope.authorizations = response.data;
      });
    };

    $scope.getPermissionsForResource = function() {
      if(!!$scope.selectedResourceType) {
        return $scope.permissionMap[$scope.selectedResourceType];
      }else {
        return [];
      }
    };

    // page controls ////////////////////////////////////
    
    $scope.show = function(fragment) {
      return fragment == $location.search().tab;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();      
      return path.indexOf(link) != -1 ? "active" : "";
    };

    // init ////////////////////////////////////

    createResourceList();
    
    if(!$location.search().resource) {
      $location.search({'resource': 0});
      $location.replace();
      $scope.title = $scope.getResource(0);
      $scope.selectedResourceType = 0;

    } else {
      $scope.title = $scope.getResource($routeParams.resource);
      $scope.selectedResourceType = $routeParams.resource;

    }

    loadAuthorizations();

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/authorization', {
      templateUrl: 'pages/authorizations.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      },
      reloadOnSearch: false
    });
  }];

  module
    .config(RouteConfig);

});
