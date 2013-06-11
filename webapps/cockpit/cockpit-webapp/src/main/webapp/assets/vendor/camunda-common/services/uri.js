ngDefine('camunda.common.services', function(module) {

  var UriProvider = function () {

    var templatePattern = "://";
    var replacements = {};

    this.replace = function(pattern, replacement) {
      replacements[pattern] = replacement;
    };

    function appUri(str) {
      var idx = str.indexOf(templatePattern);
      if (idx === -1) {
        return str;
      }

      var endIdx = idx + templatePattern.length;

      var template = str.substring(0, endIdx);
      var replacement = replacements[template];

      if (replacement === undefined) {
        return str;
      }

      var replaced = replacement + str.substring(endIdx);

      return replaced;
    }

    this.$get = [function() {
      return {
        appUri: appUri
      };
    }];
  };

  module
    .provider("Uri", UriProvider);
});
