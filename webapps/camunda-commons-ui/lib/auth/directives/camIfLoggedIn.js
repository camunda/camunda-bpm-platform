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

module.exports = [
  '$animate',
  '$rootScope',
  function($animate, $rootScope) {
    return {
      transclude: 'element',
      priority: 1000,
      terminal: true,
      restrict: 'A',
      compile: function(element, attr, transclude) {
        return function($scope, $element) {
          var childElement, childScope;

          function redraw(show) {
            if (childElement) {
              $animate.leave(childElement);
              childElement = undefined;
            }
            if (childScope) {
              childScope.$destroy();
              childScope = undefined;
            }
            if (show) {
              childScope = $scope.$new();
              transclude(childScope, function(clone) {
                childElement = clone;
                $animate.enter(clone, $element.parent(), $element);
              });
            }
          }

          $scope.$on('authentication.changed', function(e, authentication) {
            redraw(authentication);
          });

          redraw($rootScope.authentication);
        };
      }
    };
  }
];
