ngDefine('camunda.common.directives.ifLoggedIn', [
  'module:camunda.common.services.authentication:../services/Authentication'
], function(module) {

  var loggedInDirective = [ '$animator', 'Authentication', function($animator, Authentication) {
    return {
      transclude: 'element',
      priority: 1000,
      terminal: true,
      restrict: 'A',
      compile: function (element, attr, transclude) {
        return function ($scope, $element, $attr) {
          var animate = $animator($scope, $attr);
          var childElement, childScope;

          function watchUser() {
            return Authentication.user;
          }

          $scope.$watch(watchUser, function ifLoggedInWatchAction(value) {
            if (childElement) {
              animate.leave(childElement);
              childElement = undefined;
            }
            if (childScope) {
              childScope.$destroy();
              childScope = undefined;
            }
            if (!!value) {
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

  var loggedOutDirective = [ '$animator', 'Authentication', function($animator, Authentication) {
    return {
      transclude: 'element',
      priority: 1000,
      terminal: true,
      restrict: 'A',
      compile: function (element, attr, transclude) {
        return function ($scope, $element, $attr) {
          var animate = $animator($scope, $attr);
          var childElement, childScope;

          function watchUser() {
            return !Authentication.user;
          }

          $scope.$watch(watchUser, function ifLoggedInWatchAction(value) {
            if (childElement) {
              animate.leave(childElement);
              childElement = undefined;
            }
            if (childScope) {
              childScope.$destroy();
              childScope = undefined;
            }
            if (!!value) {
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

  module.directive('ifLoggedIn', loggedInDirective);
  module.directive('ifLoggedOut', loggedOutDirective);
});