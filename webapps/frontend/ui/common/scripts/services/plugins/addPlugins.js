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

var addApiAttributes = require('./getApiAttributes');
var getPassthroughData = require('./getPassthroughData');
var loadPlugins = require('./loadPlugins');

module.exports = async function(config, module, appName) {
  const plugins = await loadPlugins(config, appName);

  plugins.forEach(plugin => {
    const pluginDirectiveUID = Math.random()
      .toString(36)
      .substring(2);

    // overlay function for diagram overlay plugins
    plugin.overlay = [
      'control',
      '$scope',
      ({getViewer}, scope) => {
        plugin.render(
          getViewer(),
          addApiAttributes(
            getPassthroughData(plugin.pluginPoint, scope, appName),
            config.csrfCookieName
          ),
          scope // The 'scope' argument is deprecated and should not be used - it will be removed in future releases
        );

        scope.$on('$destroy', () => {
          plugin.unmount && plugin.unmount();
        });
      }
    ];

    module.directive('pluginBridge' + pluginDirectiveUID, [
      function() {
        return {
          link: function(scope, element) {
            const isolatedContainer = document.createElement('div');
            plugin.render(
              isolatedContainer,
              addApiAttributes(
                getPassthroughData(plugin.pluginPoint, scope, appName),
                config.csrfCookieName,
                appName
              ),
              scope // The 'scope' argument is deprecated and should not be used - it will be removed in future releases
            );

            element[0].appendChild(isolatedContainer);
            scope.$on('$destroy', () => {
              plugin.unmount && plugin.unmount();
            });
          }
        };
      }
    ]);

    module.config([
      'ViewsProvider',
      function(ViewsProvider) {
        ViewsProvider.registerDefaultView(plugin.pluginPoint, {
          ...plugin.properties, // For backwards-compatibility with 'label' property
          ...plugin,
          template: `<div plugin-bridge${pluginDirectiveUID} />`
        });
      }
    ]);

    if (plugin.pluginPoint === `${appName}.route`) {
      module.config([
        '$routeProvider',
        function($routeProvider) {
          $routeProvider.when(plugin.properties.path, {
            template: `<div plugin-bridge${pluginDirectiveUID} />`,
            controller: [
              '$scope',
              function($scope) {
                $scope.$root.showBreadcrumbs = false;
              }
            ],
            authentication: 'required'
          });
        }
      ]);
    }
  });
};
