define([
  'text!./cam-cockpit-deployments.html',
  'text!./../modals/cam-cockpit-delete-deployment-modal.html',
  'angular'
], function(
  template,
  modalTemplate,
  angular
) {
  'use strict';

  return [function() {

    return {

      restrict: 'A',
      scope: {
        deploymentsData: '='
      },

      template: template,

      controller: [
        '$scope',
        '$location',
        '$modal',
        'search',
      function (
        $scope,
        $location,
        $modal,
        search
      ) {

        var deploymentsListData = $scope.deploymentsListData = $scope.deploymentsData.newChild($scope);


        // utilities /////////////////////////////////////////////////////////////////

        var updateSilently = function(params) {
          search.updateSilently(params);
        }

        var getPropertyFromLocation = function(property) {
          var search = $location.search() || {};
          return search[property] || null;
        }


        // pagination ////////////////////////////////////////////////////////////////

        $scope.pageNum = 1;
        $scope.pageSize = null;
        $scope.totalItems = 0;

        var pageChange = $scope.pageChange = function (page) {
          // update query
          updateSilently({
            deploymentsPage: page,
            editMode: null
          });
          deploymentsListData.changed('deploymentsPagination');
        };

        $scope.resetPage = function() {
          pageChange(null);
        };


        // observe data ///////////////////////////////////////////////////////////////

        $scope.state = deploymentsListData.observe('deployments', function(deployments) {
          $scope.deployments = deployments;
        });

        $scope.state = deploymentsListData.observe([ 'deploymentsQuery', 'deploymentsCount', function(query, count) {
          $scope.pageSize = query.maxResults;
          $scope.pageNum = (query.firstResult / $scope.pageSize) + 1;
          $scope.totalItems = count;
        }]);

        deploymentsListData.observe('currentDeployment', function (currentDeployment) {
          $scope.currentDeployment = currentDeployment;
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
            deployment: deployment.id,
          });
          deploymentsListData.changed('currentDeployment');
        };

        var isFocused = $scope.isFocused = function(deployment) {
          return deployment && $scope.currentDeployment && deployment.id === $scope.currentDeployment.id;
        };

        // delete deployment ////////////////////////////////////////////////////////

        $scope.deleteDeployment = function ($event, deployment) {
          $event.stopPropagation();

          $modal.open({
            controller: 'camDeleteDeploymentModalCtrl',
            template: modalTemplate,
            resolve: {
              'deploymentsListData': function() { return deploymentsListData; },
              'deployment': function() { return deployment; }
            }
          }).result.then(function() {
            deploymentsListData.changed('deployments');
          });

        };

      }]
    };
  }];
});
