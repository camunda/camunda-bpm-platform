ngDefine('camunda.common.directives.showIfAuthorized', [
  'module:camunda.common.resources.authorization:../resources/authorizationResource', 
], function(module) {

  var builtInPermissions = {
    "none": 0,
    "all": 2147483647,
    "read": 2,
    "update": 4,
    "create": 8,
    "delete": 16,
    "access": 32  
  };

  var builtInResources = {
    "user": 1,
    "group": 2,
    "group membership": 3
  };

  var mapParameters = function(permissionName, resource, resourceId) {

    var request = {};

    request.permissionName = permissionName;
    request.permissionValue = builtInPermissions[permissionName];
    request.resourceName = resource;
    request.resourceType = builtInResources[resource];
    
    if(!!resourceId) {
      request.resourceId = resourceId;
    }
    
    return request;
  };

  var showIfAuthorized = [ '$animator', 'AuthorizationResource', function($animator, AuthorizationResource) {
    return {
      transclude: 'element',
      priority: 1000,
      terminal: true,
      restrict: 'A',
      compile: function (element, attr, transclude) {
        return function ($scope, $element, $attr) {
          var animate = $animator($scope, $attr);
          var childElement, childScope;

          var permission = attr['authPermission'];
          var resourceName = attr['authResourceName'];
          var resourceId = $scope.$eval(attr['authResourceId']);
          
          AuthorizationResource.check(mapParameters(permission, resourceName, resourceId)).$then(function(response) {

            if (childElement) {
              animate.leave(childElement);
              childElement = undefined;
            }

            if (childScope) {
              childScope.$destroy();
              childScope = undefined;
            }

            if (!!response.data.authorized) {
              childScope = $scope.$new();
              transclude(childScope, function (clone) {
                childElement = clone;
                animate.enter(clone, $element.parent(), $element);
              });
            }      

          });          

        }
      }
    }
  }];

  module.directive('showIfAuthorized', showIfAuthorized);
});