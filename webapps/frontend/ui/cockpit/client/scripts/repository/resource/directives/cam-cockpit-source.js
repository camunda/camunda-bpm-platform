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

var template = require('./cam-cockpit-source.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');
require('camunda-commons-ui/vendor/prism');

module.exports = [
  '$window',
  function($window) {
    return {
      restrict: 'A',

      scope: {
        name: '=',
        source: '='
      },

      template: template,

      link: function($scope, $element) {
        var Prism = $window.Prism;

        var Extensions = {
          js: 'javascript',
          html: 'markup',
          xml: 'markup',
          py: 'python',
          rb: 'ruby',
          bpmn: 'markup',
          cmmn: 'markup',
          dmn: 'markup'
        };

        var name = $scope.name;

        $scope.extension = function() {
          if (name) {
            var extension = (name.match(/\.([\w-]+)$/) || ['', ''])[1];
            extension = extension && extension.toLowerCase();
            return Extensions[extension] || extension;
          }
        };

        $scope.$watch('source', function(source) {
          if (source) {
            var codeElement = angular.element('code', $element);
            Prism.highlightElement(codeElement[0]);
          }
        });
      }
    };
  }
];
