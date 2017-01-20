'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-deployments.html', 'utf8');

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      deploymentsData: '='
    },

    template: template,

    controller: [
      '$scope',
      '$location',
      'search',
      'Notifications',
      function(
        $scope,
        $location,
        search,
        Notifications
      ) {

        var deploymentsListData = $scope.deploymentsListData = $scope.deploymentsData.newChild($scope);


        // utilities /////////////////////////////////////////////////////////////////

        var updateSilently = function(params) {
          search.updateSilently(params);
        };


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


        // pagination ////////////////////////////////////////////////////////////////

        $scope.pageNum = 1;
        $scope.pageSize = null;
        $scope.totalItems = 0;

        var pageChange = $scope.pageChange = function(page) {
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

        deploymentsListData.observe('currentDeployment', function(currentDeployment) {
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
