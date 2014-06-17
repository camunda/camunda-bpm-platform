/* global ngDefine: false */
ngDefine('camunda.common.services', function(module) {

  module.filter('escape', function() {
    return function(str) {
      // see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent

      // we double escape the / character => / is escaped as '%2F', 
      // we additionally escape '%' as '%25'
      return encodeURIComponent(str).replace(/%2F/g, "%252F")
             .replace(/[!'()]/g, escape)
             .replace(/\*/g, "%2A");
    };
  });

});
