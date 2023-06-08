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
require('mousetrap');

var helpLinkTemplate = require('./cam-tasklist-shortcut-help-plugin.html?raw');
var showHelpTemplate = require('./modals/cam-tasklist-shortcut-help.html?raw');

var Controller = [
  '$scope',
  '$uibModal',
  function($scope, $modal) {
    var mousetrap = require('mousetrap');

    if (
      typeof window.camTasklistConf !== 'undefined' &&
      window.camTasklistConf.shortcuts
    ) {
      $scope.shortcuts = window.camTasklistConf.shortcuts;

      for (var key in window.camTasklistConf.shortcuts) {
        var shortcut = window.camTasklistConf.shortcuts[key];
        mousetrap.bind(
          shortcut.key,
          (function(key) {
            return function() {
              $scope.$root.$broadcast('shortcut:' + key);
            };
          })(key)
        );
      }
    }

    $scope.showHelp = function() {
      var modalInstance = $modal.open({
        // creates a child scope of a provided scope
        scope: $scope,
        windowClass: 'shortcut-modal',
        size: 'lg',
        template: showHelpTemplate
      });

      modalInstance.result.then(
        function() {
          document.querySelector('a.showShortcutHelp').focus();
        },
        function() {
          document.querySelector('a.showShortcutHelp').focus();
        }
      );
    };
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'shortcut-help',
    template: helpLinkTemplate,
    controller: Controller,
    priority: 300
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
