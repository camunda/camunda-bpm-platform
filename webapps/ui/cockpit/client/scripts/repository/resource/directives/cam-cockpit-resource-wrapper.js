'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-resource-wrapper.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [ function() {

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
      '$location',
      'Views',
      'Notifications',
      'search',
      function(
        $scope,
        $q,
        $location,
        Views,
        Notifications,
        search
      ) {

        // utilities ///////////////////////////////////////////////////////////////////

        var errorNotification = function(src, err) {
          if (err.message) {
            var idx = err.message.indexOf('<-');
            if (idx !== -1) {
              err.message = err.message.split('<-')[1].trim();
            }
          }
          Notifications.addError({
            status: src,
            message: (err ? err.message : ''),
            exclusive: true,
            scope: $scope
          });
        };

        var enhanceErrorMessage = function(msg) {
          if (msg) {

            if(msg.indexOf('does not exist') === -1) {
              return 'The deployment resource does not exist anymore';
            }

          }
          return 'Could not load deployment resource';
        };

        var clearResource = function() {
          var search = $location.search() || {};
          delete search.resource;
          delete search.resourceName;
          $location.search(angular.copy(search));
          $location.replace();
        };

        // fields /////////////////////////////////////////////////////////////////////

        var resourceData = $scope.resourceData = $scope.resourceDetailsData.newChild($scope);

        var PLUGIN_DETAILS_COMPONENT = 'cockpit.repository.resource.detail';


        // observe /////////////////////////////////////////////////////////////////////

        resourceData.observe('currentDeployment', function(deployment) {
          $scope.deployment = deployment;
        });

        $scope.resourceState = resourceData.observe([ 'resource', 'binary', function(resource) {
          $scope.resource = resource;
        }]);

        $scope.$watch('resourceState.$error', function(err) {
          if (err) {
            var src = enhanceErrorMessage(err.message);
            errorNotification(src, err);
            clearResource();
          }
        });

        // plugins ///////////////////////////////////////////////////////////////////////

        $scope.resourceVars = { read: [ 'control', 'deployment', 'resource', 'resourceData' ] };
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
