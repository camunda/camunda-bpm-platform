define(['text!./system.html'], function(template) {
  'use strict';
  var Controller = [
    '$scope',
    '$location',
    '$routeParams',
    'Views',
    function($scope,
             $location,
             $routeParams,
             Views) {

    $scope.systemSettingsProviders = Views.getProviders({ component: 'admin.system'});

    var selectedProviderId = $routeParams.section;
    if(!!selectedProviderId) {
      $scope.activeSettingsProvier = Views.getProviders({
        component: 'admin.system',
        id: $routeParams.section
      })[0];
    }


    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? "active" : "";
    };

  }];

  return [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/system', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }];
});
