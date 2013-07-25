ngDefine('camunda.common.services', function(module) {

  var InterceptorFactory = function($rootScope, $q, RequestStatus) {
    return function(promise) {

      RequestStatus.setBusy(true);

      function success(response) {
        RequestStatus.setBusy(false);
        return promise;
      };

      function error(response) {
        RequestStatus.setBusy(false);

        var responseError = {
          status: parseInt(response.status),
          response: response,
          data: response.data
        };

        $rootScope.$broadcast("responseError", responseError);

        return $q.reject(response);
      }

      return promise.then(success, error);
    };
  };

  InterceptorFactory.$inject = [ "$rootScope", "$q", "RequestStatus" ];

  module.factory("httpStatusInterceptor", InterceptorFactory);

  return module;
});