ngDefine('camunda.common.services', function(module) {

  var HttpStatusInterceptorFactory = 
    [ '$rootScope', '$q', 'RequestStatus', 
      function($rootScope, $q, RequestStatus) {
    
    return function(promise) {

      RequestStatus.setBusy(true);

      function success(response) {
        RequestStatus.setBusy(false);
        
        return response;
      }

      function error(response) {
        RequestStatus.setBusy(false);

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