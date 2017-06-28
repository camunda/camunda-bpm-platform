'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-deployments.html', 'utf8');
var searchConfigJSON = fs.readFileSync(__dirname + '/cam-cockpit-deployments-search-plugin-config.json', 'utf8');

module.exports = [function() {
  return {
    restrict: 'A',
    scope: {
      deploymentsData: '=',
      totalItems: '=',
      deployments: '='
    },
    template: template,
    controller: [
      '$scope',
      '$location',
      '$rootScope',
      'search',
      'Notifications',
      'camAPI',
      function(
        $scope,
        $location,
        $rootScope,
        search,
        Notifications,
        camAPI
      ) {
        var Deployment = camAPI.resource('deployment');
        var deploymentsListData = $scope.deploymentsListData = $scope.deploymentsData.newChild($scope);
        $scope.searchConfig = JSON.parse(searchConfigJSON);
        $scope.loadingState = 'INITIAL';

        // control ///////////////////////////////////////////////////////////////////
        var control = $scope.control = {};
        control.addMessage = function(status, msg, unsafe) {
          Notifications.addMessage({
            status: status,
            message: msg,
            scope: $scope,
            unsafe: unsafe
          });
        };

        $scope.onSearchChange = function(query, pages) {
          $scope.loadingState = 'LOADING';
          var pagination = {
            firstResult: (pages.current - 1) * pages.size,
            maxResults: pages.size
          };

          return Deployment.list(
              Object.assign(query, pagination, $scope.deploymentsSorting)
            )
            .then(function(res) {
              $scope.deployments = res.items;

              $scope.loadingState = 'LOADED';

              return res.count;
            })
            .catch(function() {
              $scope.loadingState = 'ERROR';
            });
        };

        // observe data ///////////////////////////////////////////////////////////////
        deploymentsListData.observe('currentDeployment', function(currentDeployment) {
          $scope.currentDeployment = currentDeployment;
        });

        deploymentsListData.observe('deploymentsSorting', function(deploymentsSorting) {
          $scope.deploymentsSorting = deploymentsSorting;

          $rootScope.$broadcast('cam-common:cam-searchable:query-force-change');
        });

        // selection ////////////////////////////////////////////////////////////////
        $scope.focus = function(deployment) {
          if (!isFocused(deployment)) {
            search.updateSilently({
              resource: null,
              resourceName: null,
              viewbox: null,
              editMode: true
            });
          }

          search.updateSilently({
            deployment: deployment.id
          });
          deploymentsListData.changed('currentDeployment');
        };

        var isFocused = $scope.isFocused = function(deployment) {
          return deployment && $scope.currentDeployment && deployment.id === $scope.currentDeployment.id;
        };

      }]
  };
}];
