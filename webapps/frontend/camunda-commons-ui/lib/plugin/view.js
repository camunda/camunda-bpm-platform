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
// module is passed by the "loader" (main.js)
module.exports = function(module) {
  module.directive('view', [
    '$q',
    '$http',
    '$templateCache',
    '$anchorScroll',
    '$compile',
    '$controller',
    function($q, $http, $templateCache, $anchorScroll, $compile, $controller) {
      return {
        restrict: 'ECA',
        terminal: true,
        link: function(scope, element, attrs) {
          var lastScope;

          scope.$watch(attrs.provider, update);

          function destroyLastScope() {
            if (lastScope) {
              lastScope.$destroy();
              lastScope = null;
            }
          }

          function clearContent() {
            element.html('');
            destroyLastScope();
          }

          function getTemplate(viewProvider) {
            var template = viewProvider.template;
            if (template) {
              return template;
            }

            var url = viewProvider.url;
            return $http
              .get(url, {cache: $templateCache})
              .then(function(response) {
                return response.data;
              })
              .catch(angular.noop);
          }

          function update() {
            var viewProvider = scope.$eval(attrs.provider);
            var viewVars = scope.$eval(attrs.vars) || {};

            if (!viewProvider) {
              clearContent();
              return;
            }

            $q.when(getTemplate(viewProvider)).then(
              function(template) {
                element.html(template);
                destroyLastScope();

                var link = $compile(element.contents()),
                  locals = {},
                  controller;

                lastScope = scope.$new(true);

                if (viewVars) {
                  if (viewVars.read) {
                    angular.forEach(viewVars.read, function(e) {
                      // fill read vars initially
                      lastScope[e] = scope[e];

                      scope.$watch(e, function(newValue) {
                        lastScope[e] = newValue;
                      });
                    });
                  }

                  if (viewVars.write) {
                    angular.forEach(viewVars.write, function(e) {
                      lastScope.$watch(e, function(newValue) {
                        scope[e] = newValue;
                      });
                    });
                  }
                }

                if (viewProvider.controller) {
                  locals.$scope = lastScope;
                  controller = $controller(viewProvider.controller, locals);
                  element
                    .children()
                    .data('$ngControllerController', controller);
                }

                link(lastScope);
                lastScope.$emit('$pluginContentLoaded');

                // $anchorScroll might listen on event...
                $anchorScroll();
              },
              function(error) {
                clearContent();

                throw error;
              }
            );
          }
        }
      };
    }
  ]);
};
