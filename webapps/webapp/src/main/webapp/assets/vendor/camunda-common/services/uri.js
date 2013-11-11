ngDefine('camunda.common.services.uri', [ 'angular' ], function(module, angular) {

  var UriProvider = function() {

    var TEMPLATES_PATTERN = /[\w]+:\/\/|:[\w]+/g;

    var replacements = {};

    /**
     * Specifies a pattern and its replacement on the UriProvider
     *
     * @description
     * Used to register a new replacement for app uris.
     * Typically the registration of these replacements takes place in one distinct place (i.e. the app main file).
     *
     * Two kinds of patterns are supported, <code>:foo</code> (as path parameter) and <code>foo://</code> (as path prefix).
     *
     * In addition to a plain replacement string, a function may be specified that provides the replacement.
     *
     *  <pre>
     *    module.config(function(UriProvider, $routeParams) {
     *      UriProvider.register(':foo', 'bar');
     *      UriProvider.register('asdf://', function() {
     *        return $routeParams.bar;
     *      });
     *    });
     *  </pre>
     *
     * @param {string} pattern
     * @param {string|Function} replacement string or function
     */
    this.replace = function(pattern, replacement) {
      replacements[pattern] = replacement;
    };

    this.$get = [ '$injector', function($injector) {
      return {
        /**
         * @function
         *
         * Transforms a app uri using the configured replacement rules.
         *
         * @param {string} str the uri to transform
         * @returns {string} the replaced string
         */
        appUri: function appUri(str) {
          var replaced = str.replace(TEMPLATES_PATTERN, function(template, index, str) {

            var replacement = replacements[template];
            if (replacement === undefined) {
              return template;
            }

            // allow replace support for functions
            if (angular.isFunction(replacement) || angular.isArray(replacement)) {
              replacement = $injector.invoke(replacement);
            }

            return replacement;
          });

          return replaced;
        }
      };
    }];
  };

  /**
   * A filter to be used in views to replace tokens in a url
   *
   * Example: 
   * <pre>
   *   <a ng-href="{{ 'engine://foo' | uri }}">Perform Foo</a>
   * </pre>
   */
  var UriFilter = [ 'Uri', function(Uri) {
    return function(input) {
      return Uri.appUri(input);
    }
  }];

  module
    .provider("Uri", UriProvider)
    .filter('uri', UriFilter);
});
