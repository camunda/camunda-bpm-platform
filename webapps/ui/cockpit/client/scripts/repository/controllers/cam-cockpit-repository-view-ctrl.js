'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope',
  '$q',
  '$location',
  '$timeout',
  'search',
  'dataDepend',
  'page',
  'camAPI',
  function(
    $scope,
    $q,
    $location,
    $timeout,
    search,
    dataDepend,
    page,
    camAPI
  ) {
    $scope.$root.showBreadcrumbs = false;

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      label: 'Deployments'
    });

    page.titleSet('Deployments');

    // utilities /////////////////////////////////////////////////////////////////

    var updateSilently = function(params) {
      search.updateSilently(params);
    };

    var getPropertyFromLocation = function(property) {
      var search = $location.search() || {};
      return search[property] || null;
    };


    // fields ///////////////////////////////////////////////////

    var maxResults = 15;

    var Deployment = camAPI.resource('deployment');

    // init data depend for deployments data
    var repositoryData = $scope.repositoryData =  dataDepend.create($scope);

    var deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
    var deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
    var deploymentsPage = getPropertyFromLocation('deploymentsPage');
    var deploymentId = getPropertyFromLocation('deployment');
    var resourceId = getPropertyFromLocation('resource');
    var resourceName = getPropertyFromLocation('resourceName');


    // provide data //////////////////////////////////////////////////////////////////

    repositoryData.provide('deploymentsSearchQuery', {});

    repositoryData.provide('deploymentsPagination', function() {
      var deferred = $q.defer();
      deploymentsPage = getPropertyFromLocation('deploymentsPage');

      // wait for angular to initialize the controllers, so that the search
      // works properly (for example after a page refresh)
      $timeout(function() {
        deferred.resolve({
          firstResult: ((deploymentsPage || 1) - 1) * maxResults,
          maxResults: maxResults
        });
      });

      return deferred.promise;
    });

    repositoryData.provide('deploymentsSorting', function() {
      deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
      deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
      return {
        sortBy: deploymentsSortBy || 'deploymentTime',
        sortOrder: deploymentsSortOrder || 'desc'
      };
    });

    // responsible for refreshing the deployments list top position.
    // nope. css can't do that (unless we drop support for IE9 and use flexbox)
    function updateDeploymentsListTop() {
      var filterContainer = angular.element('[ng-controller="camDeploymentsCtrl"] .content view');
      $scope.deploymentsListTop = (
                                    (filterContainer[0].offsetTop * 1) +
                                    Math.max(49, (filterContainer.height() || 0))
                                  ) + 'px';
    }
    $timeout(updateDeploymentsListTop, 200);

    repositoryData.provide('deploymentsQuery', [ 'deploymentsSearchQuery', 'deploymentsPagination', 'deploymentsSorting', function(query, pagination, sorting) {
      var deferred = $q.defer();

      // wait for angular to initialize the controllers, so that the search
      // works properly (for example after a page refresh)
      $timeout(function() {
        query = query || {};
        deferred.resolve(angular.extend({}, query, pagination, sorting));

        updateDeploymentsListTop();
      });

      return deferred.promise;
    }]);

    repositoryData.provide([ 'deployments', 'deploymentsCount' ], [ 'deploymentsQuery', function(query) {
      var deferred = $q.defer();

      Deployment.list(query, function(err, res) {

        if (err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve([ res.items, res.count ]);
        }

      });

      return deferred.promise;
    }]);

    repositoryData.provide('currentDeployment', ['deployments', function(deployments) {

      deployments = deployments || [];

      var focused;
      var _deploymentId = getPropertyFromLocation('deployment');

      for (var i = 0, deployment; (deployment = deployments[i]); i++) {

        if (_deploymentId === deployment.id) {
          focused = deployment;
          break;
        }
          // auto focus first deployment
        if(!focused) {
          focused = deployment;
        }
      }

      if (focused) {
        deploymentId = focused.id;

        if (focused.id !== _deploymentId) {
          updateSilently({
            deployment: focused.id,
            resource: null,
            viewbox: null,
            editMode: null
          });
          $location.replace();
        }

      }
      else {
        updateSilently({
          deployment: null,
          resource: null,
          viewbox: null,
          editMode: null
        });
        $location.replace();
      }

      return angular.copy(focused);
    }]);

    repositoryData.provide('resources', [ 'currentDeployment', function(currentDeployment) {
      var deferred = $q.defer();

      if(!currentDeployment || currentDeployment.id === null) {
        deferred.resolve(null);
      }
      else {
        Deployment.getResources(currentDeployment.id, function(err, res) {
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

    repositoryData.provide('resourceId', [ 'resources', function(resources) {
      resourceId = getPropertyFromLocation('resource');
      resourceName = getPropertyFromLocation('resourceName');

      if (resourceId) {
        return {
          resourceId: resourceId
        };
      }
      else if (resourceName) {
        resources = resources || [];
        for(var i=0, resource; (resource = resources[i]); i++) {
          if (resource.name === resourceName) {
            return {
              resourceId: resource.id
            };
          }
        }
      }

      return {
        resourceId: null
      };

    }]);

    repositoryData.provide('resource', ['resourceId', 'currentDeployment', function(resourceId, deployment) {
      var deferred = $q.defer();

      resourceId = resourceId.resourceId;

      if(typeof resourceId !== 'string') {
        deferred.resolve(null);
      }
      else if (!deployment || deployment.id === null) {
        deferred.resolve(null);
      }
      else {
        Deployment.getResource(deployment.id, resourceId, function(err, res) {
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


    $scope.$on('$routeChanged', function() {
      var oldDeploymentsSortBy = deploymentsSortBy;
      var oldDeploymentsSortOrder = deploymentsSortOrder;
      var oldDeploymentsPage = deploymentsPage;
      var oldDeploymentId = deploymentId;
      var oldResourceId = resourceId;
      var oldResourceName = resourceName;

      deploymentsSortBy = getPropertyFromLocation('deploymentsSortBy');
      deploymentsSortOrder = getPropertyFromLocation('deploymentsSortOrder');
      deploymentsPage = getPropertyFromLocation('deploymentsPage');
      deploymentId = getPropertyFromLocation('deployment');
      resourceId = getPropertyFromLocation('resource');
      resourceName = getPropertyFromLocation('resourceName');

      if (deploymentsSortBy && oldDeploymentsSortBy !== deploymentsSortBy || deploymentsSortOrder && oldDeploymentsSortOrder !== deploymentsSortOrder) {
        repositoryData.changed('deploymentsSorting');
      }

      if (deploymentsPage && oldDeploymentsPage !== deploymentsPage) {
        repositoryData.changed('deploymentsPagination');
      }
      else if (deploymentId && oldDeploymentId !== deploymentId) {
        repositoryData.changed('currentDeployment');
      }
      else if (resourceId && oldResourceId !== resourceId || resourceName && oldResourceName !== resourceName) {
        repositoryData.changed('resourceId');
      }
    });

  }];
