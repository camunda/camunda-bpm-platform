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

var template = require('./breadcrumbs.html?raw');

module.exports = [
  '$location',
  'routeUtil',
  function($location, routeUtil) {
    return {
      scope: {
        divider: '@'
      },

      restrict: 'A',

      template: template,

      link: function(scope) {
        // event triggered by the breadcrumbs service when the breadcrumbs are alterated
        scope.$on('page.breadcrumbs.changed', function(ev, breadcrumbs) {
          scope.breadcrumbs = breadcrumbs;
        });

        scope.getHref = function(crumb) {
          return routeUtil.redirectTo(
            crumb.href,
            $location.search(),
            crumb.keepSearchParams
          );
        };

        scope.selectChoice = function(evt, choice) {
          evt.preventDefault();
          $location.path(choice.href.substr(1));
        };

        scope.getActiveChoice = function(choices) {
          var label;
          choices.forEach(function(choice) {
            if (choice.active) {
              label = choice.label;
            }
          });
          return label || 'Options';
        };

        scope.sortedChoices = function(choices) {
          return choices.sort(function(a, b) {
            return a.active ? -1 : b.active ? 1 : 0;
          });
        };
      },

      controller: [
        '$scope',
        'page',
        function($scope, page) {
          // initialize the $scope breadcrumbs from the service
          $scope.breadcrumbs = page.breadcrumbsGet();
        }
      ]
    };
  }
];
