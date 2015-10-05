define([
  'angular',
  'text!./cam-cockpit-resource-wrapper.html'
], function(
  angular,
  template
) {
  'use strict';

  return [ function() {

    return {
      restrict: 'A',
      scope: {
        resourceDetailsData: '=',
        control: '=?'
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

        // fielda /////////////////////////////////////////////////////////////////////

        var Deployment = camAPI.resource('deployment');

        var resourceData = $scope.resourceData = $scope.resourceDetailsData.newChild($scope);

        var PLUGIN_DETAILS_COMPONENT = 'cockpit.repository.resource.detail';


        // observe /////////////////////////////////////////////////////////////////////

        resourceData.observe('currentDeployment', function(deployment) {
          $scope.deployment = deployment;
        });

        $scope.resourceState = resourceData.observe([ 'resource', 'binary', function(resource, binary) {
          $scope.resource = resource;
        }]);


        // plugins ///////////////////////////////////////////////////////////////////////

        $scope.resourceVars = { read: [ 'deployment', 'resource', 'resourceData' ] };
        $scope.resourceDetailTabs = Views.getProviders({ component: PLUGIN_DETAILS_COMPONENT });

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
            var provider = Views.getProvider({ component: PLUGIN_DETAILS_COMPONENT, id: selectedResourceId });
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

