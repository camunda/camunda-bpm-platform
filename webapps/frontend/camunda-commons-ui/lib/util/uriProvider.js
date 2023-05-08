/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var angular = require('camunda-bpm-sdk-js/vendor/angular');

module.exports = function() {
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

  this.$get = [
    '$injector',
    function($injector) {
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
          var replaced = str.replace(TEMPLATES_PATTERN, function(template) {
            var replacement = replacements[template];
            if (replacement === undefined) {
              return template;
            }

            // allow replace support for functions
            if (
              angular.isFunction(replacement) ||
              angular.isArray(replacement)
            ) {
              replacement = $injector.invoke(replacement);
            }

            return replacement;
          });

          return replaced;
        }
      };
    }
  ];
};
