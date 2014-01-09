ngDefine('camunda.common.services', function(module) {

  var HttpStatusInterceptorFactory = 
    [ '$rootScope', '$q', 'RequestLogger', 
      function($rootScope, $q, RequestLogger) {

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

      return promise.then(success, error);
    };
  }];

  /**
   * Register http status interceptor per default
   */
  module.config([ '$httpProvider', function($httpProvider) {
    $httpProvider.responseInterceptors.push(HttpStatusInterceptorFactory);
  }]);
});