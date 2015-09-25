define([
  'angular',
  'text!./cam-cockpit-resource.html'
], function(
  angular,
  template
) {
  'use strict';

  return [ function() {

    return {
      restrict: 'A',
      scope: {
        repositoryData: '='
      },

      template: template,

      controller : [
        '$scope',
        '$q',
        'camAPI',
        'Views',
        'search',
      function(
        $scope,
        $q,
        camAPI,
        Views,
        search
      ) {

        // setup /////////////////////////////////////////////////////////////////////

        var Deployment = camAPI.resource('deployment');

        var resourceData = $scope.resourceData = $scope.repositoryData.newChild($scope);

        resourceData.provide('resourceBinary', [ 'resource', 'currentDeployment', function(resource, deployment) {
          var deferred = $q.defer();
          
          if (!resource) {
            deferred.resolve(null);
          }
          else if (!deployment || deployment.id === null) {
            deferred.resolve(null);
          }
          else {
            Deployment.getResourceData(deployment.id, resource.id, function(err, res) {
              if(err) {
                deferred.reject(err);
              }
              else {
                deferred.resolve(res);
              }

            });
          }

          return deferred.promise;
        }]);

        resourceData.observe('currentDeployment', function(deployment) {
          $scope.deployment = deployment;
        });

        $scope.state = resourceData.observe('resource', function(resource) {
          $scope.resource = resource;
        });

        // plugins //////////////////////////////////////////////////////////////

        $scope.resourceVars = { read: [ 'deployment', 'resource', 'resourceData' ] };
        $scope.resourceDetailTabs = Views.getProviders({ component: 'cam.cockpit.repository.resouce.detail' });

        $scope.selectedResourceDetailTab = $scope.resourceDetailTabs[0];

        $scope.selectResourceDetailTab = function(tab) {
          $scope.selectedResourceDetailTab = tab;

          search.updateSilently({
            detailsTab: tab.id
          });
        };

        function setDefaultResourceDetailTab(tabs) {
          var selectedResourceId = search().detailsTab;

          if (!tabs || !tabs.length) {
            return;
          }

          if (selectedResourceId) {
            var provider = Views.getProvider({ component: 'cam.cockpit.repository.resouce.detail', id: selectedResourceId });
            if (provider && tabs.indexOf(provider) != -1) {
              $scope.selectedResourceDetailTab = provider;
              return;
            }
          }

          search.updateSilently({
            detailsTab: null
          });

          $scope.selectedResourceDetailTab = tabs[0];
        }

        setDefaultResourceDetailTab($scope.resResourceDetailTabs);

        $scope.$on('$routeChanged', function() {
          setDefaultResourceDetailTab($scope.resResourceDetailTabs);
        });

      }]
    };
  }];
});

