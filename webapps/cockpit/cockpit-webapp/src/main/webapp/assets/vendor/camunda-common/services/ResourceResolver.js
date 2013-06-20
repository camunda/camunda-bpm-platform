ngDefine('camunda.common.services.resolver', function(module) {

  // depends on "Notifications"

  var ServiceProviderFactory = [
    '$route', '$q', '$location', 'Notifications',
    function($route, $q, $location, Notifications) {

      function getByRouteParam(paramName, options) {
        var deferred = $q.defer();

        var id = $route.current.params[paramName],
            resolve = options.resolve,
            resourceName = options.name || "entity";

        function succeed(response) {
          deferred.resolve(response.resource);
        }

        function fail(errorResponse) {
          var message, replace;

          if (errorResponse.status === 400) {
            message = "No " + resourceName + " with ID " + id;
            replace = true;
          } else {
            message = "Received " + errorResponse.status + " from server.";
          }

          $location.url("/dashboard");
          if (replace) {
            $location.replace();
          }

          Notifications.addError({ status: "Failed to display " + resourceName, message: message, http: true, exclusive: [ 'http' ]});

          deferred.reject(message);
        }

        // resolve
        resolve(id).$then(succeed, fail);

        return deferred.promise;
      }

      return {
        getByRouteParam: getByRouteParam
      };
    }];

  module.factory("ResourceResolver", ServiceProviderFactory);
});