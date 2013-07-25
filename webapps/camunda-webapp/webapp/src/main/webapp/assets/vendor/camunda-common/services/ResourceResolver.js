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

        function succeed(result) {
          deferred.resolve(result);
        }

        function fail(errorResponse) {
          var message, replace;

          if (errorResponse.status === 404) {
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
        var promise = resolve(id);
        if (promise.$then) {
          promise = promise.$then(function(response) { succeed(response.resource); }, fail);
        } else 
        if (promise.then) {
          promise = promise.then(succeed, fail);
        } else {
          throw new Error("No promise returned by #resolve");
        }

        return deferred.promise;
      }

      return {
        getByRouteParam: getByRouteParam
      };
    }];

  module.factory("ResourceResolver", ServiceProviderFactory);
});