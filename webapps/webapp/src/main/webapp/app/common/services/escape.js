/* global ngDefine: false */
ngDefine('camunda.common.services', function(module) {

  module.filter('escape', function() {
    return function(str) {
      // see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
      return encodeURIComponent(str).replace(/[!'()]/g, escape).replace(/\*/g, "%2A");
    };
  });

});
