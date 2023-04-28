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

var angular = require('camunda-commons-ui/vendor/angular');

function checkActive(plugin, path) {
  return path.indexOf(plugin.id) > -1 || path.indexOf(plugin.path) > -1;
}

module.exports = [
  '$scope',
  '$injector',
  '$location',
  'Views',
  function($scope, $injector, $location, Views) {
    $scope.navbarVars = {read: []};

    $scope.menuActions = [];
    $scope.dropdownActions = [];

    Views.getProviders({component: 'cockpit.navigation'}).forEach(function(
      plugin
    ) {
      if (angular.isArray(plugin.access)) {
        var fn = $injector.invoke(plugin.access);

        fn(function(err, access) {
          if (err) {
            throw err;
          }

          plugin.accessible = access;
        });
      }

      // accessible by default in case there's no callback
      else {
        plugin.accessible = true;
      }

      // "Legacy" Plugins
      if (!plugin.render) {
        plugin.template = `<a ng-href="${plugin.pagePath}">
        {{'${plugin.label}' | translate}}</a>`;
      }

      (plugin.priority >= 0 ? $scope.menuActions : $scope.dropdownActions).push(
        plugin
      );
    });

    $scope.activeClass = function(plugin) {
      var path = $location.absUrl();
      return (typeof plugin.checkActive === 'function'
      ? plugin.checkActive(path)
      : checkActive(plugin, path))
        ? 'active'
        : '';
    };
  }
];
