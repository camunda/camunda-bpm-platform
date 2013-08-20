ngDefine('camunda.common.services', function(module) {

  var HttpStatusInterceptorFactory = 
    [ '$rootScope', '$q', '$injector', 'RequestLogger', 
      function($rootScope, $q, $injector, RequestLogger) {

    return function(promise) {

      RequestLogger.logStarted();

      function success(response) {
        RequestLogger.logFinished();
        return response;
      }

      function error(response) {
        RequestLogger.logFinished();

        var httpError = {
          status: parseInt(response.status),
          response: response,
          data: response.data
        };

        $rootScope.$broadcast('httpError', httpError);

        return $q.reject(response);
      }

      function updateAuthentication(fn) {

        return function(response) {
          var Authentication = $injector.get('Authentication');

          var authorizedUser = response.headers('X-Authorized-User');
          var authorizedApps = response.headers('X-Authorized-Apps');

          Authentication.update(authorizedUser ? {
            name: authorizedUser,
            authorizedApps: authorizedApps ? authorizedApps.split(/,/) : [] 
          } : null);

          return fn(response);
        };
      }

      return promise.then(updateAuthentication(success), error);
    };
  }];

  /**
   * Register http status interceptor per default
   */
  module.config([ '$httpProvider', function($httpProvider) {
    $httpProvider.responseInterceptors.push(HttpStatusInterceptorFactory);
  }]);
});