ngDefine('camunda.common.directives.showIfAuthorized', [
  'module:camunda.common.resources.authorization:../resources/authorizationResource',
], function(module) {

  var builtInResources = {
    "application": 0,
    "user": 1,
    "group": 2,
    "group membership": 3,
    "authorization": 4,
  };

  var mapParameters = function(permissionName, resource, resourceId) {

    var request = {};

    request.permissionName = permissionName;
    request.resourceName = resource;
    request.resourceType = builtInResources[resource];

    if(!!resourceId) {
      request.resourceId = resourceId;
    }

    return request;
  };

  var showIfAuthorized = [ '$animate', 'AuthorizationResource', function($animate, AuthorizationResource) {
    return {
      transclude: 'element',
      priority: 1000,
      terminal: true,
      restrict: 'A',
      compile: function (element, attr, transclude) {
        return function ($scope, $element, $attr) {

          // var animate = $animate($scope, $attr);
          var childElement, childScope;

          var permission = attr['authPermission'];
          var resourceName = attr['authResourceName'];
          var resourceId = $scope.$eval(attr['authResourceId']);

          AuthorizationResource.check(mapParameters(permission, resourceName, resourceId)).$promise.then(function(response) {

            if (childElement) {
              // animate.leave(childElement);
              $animate.leave(childElement);
              childElement = undefined;
            }

            if (childScope) {
              childScope.$destroy();
              childScope = undefined;
            }

            // if (!!response.data.authorized) {
            if (!!response.authorized) {
              childScope = $scope.$new();
              transclude(childScope, function (clone) {
                childElement = clone;
                // animate.enter(clone, $element.parent(), $element);
                $animate.enter(clone, $element.parent(), $element);
              });
            }

          });

        }
      }
    }
  }];

  module.directive('showIfAuthorized', showIfAuthorized);
});
