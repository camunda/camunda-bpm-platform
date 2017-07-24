'use strict';

module.exports = [
  '$location', 'routeUtil',
  function($location, routeUtil) {
    return function(deployment, resource) {
      var path = '#/repository';

      var searches = {
        deployment: deployment.id,
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deployment.id
        }])
      };

      if (resource) {
        searches.resourceName = resource.name;
      }

      var searchParams = $location.search() || {};
      if (searchParams['deploymentsSortBy']) {
        searches['deploymentsSortBy'] = searchParams['deploymentsSortBy'];
      }

      if (searchParams['deploymentsSortOrder']) {
        searches['deploymentsSortOrder'] = searchParams['deploymentsSortOrder'];
      }

      return routeUtil.redirectTo(path, searches, [
        'deployment',
        'resourceName',
        'deploymentsQuery',
        'deploymentsSortOrder',
        'deploymentsSortBy'
      ]);
    };
  }
];
