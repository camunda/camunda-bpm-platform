/* global ngDefine: false */
ngDefine('camunda.common.services', function(module) {

  module.config([ '$httpProvider', '$windowProvider', function($httpProvider, $windowProvider) {
      var window = $windowProvider.$get();
      var uri = window.location.href;

      var match = uri.match(/app\/(\w+)\/(\w+)\//);
      if (match) {
        $httpProvider.defaults.headers.get = {'X-Authorized-Engine' : match[2] };
      } else {
        throw new Error('no process engine selected');
      }
  }]);

});
