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
var fs = require('fs');
var Clipboard = require('clipboard');

var template = require('./cam-widget-clipboard.html')();

module.exports = [
  '$timeout',
  '$translate',
  function($timeout, $translate) {
    return {
      transclude: true,
      template: template,
      scope: {
        value: '=camWidgetClipboard'
      },

      link: function($scope, element, attrs) {
        var cb;

        $scope.noTooltip = typeof attrs.noTooltip !== 'undefined';
        $scope.copyStatus = null;
        $scope.icon = attrs.icon || 'glyphicon-copy';

        $scope.$watch('value', function() {
          $scope.tooltipText =
            attrs.tooltipText ||
            $translate.instant('CAM_WIDGET_COPY', {value: $scope.value});
        });

        var _top;
        function restore() {
          $scope.$apply();
          _top = $timeout(
            function() {
              $scope.copyStatus = null;
            },
            1200,
            true
          );
        }

        function handleResize() {
          var content = element[0].querySelector('[ng-transclude]');
          var icon = element[0].querySelector('a.glyphicon-copy');
          var elementStyle = window.getComputedStyle(element[0]);

          var contentWidth = 1;
          var containerWidth = 0;
          if (content && icon) {
            contentWidth = content.offsetWidth + icon.offsetWidth;
            containerWidth =
              parseInt(elementStyle.width) -
              parseInt(elementStyle.paddingRight) -
              parseInt(elementStyle.paddingLeft);
          }

          if (contentWidth - containerWidth > 0) {
            if (content.className.indexOf('resize') === -1)
              content.className += ' resize';
          } else {
            content.className = content.className.replace(' resize', '');
          }
        }

        // needed because otherwise the content of `element` is not rendered yet
        // and `querySelector` is then not available
        $timeout(function() {
          var link = element[0].querySelector('a.' + $scope.icon);
          if (!link) {
            return;
          }

          window.addEventListener('resize', handleResize);
          handleResize();

          cb = new Clipboard(link, {
            text: function() {
              return $scope.value;
            }
          });

          cb.on('success', function() {
            $scope.copyStatus = true;
            restore();
          });

          cb.on('error', function() {
            $scope.copyStatus = false;
            restore();
          });
        });

        $scope.$on('$destroy', function() {
          window.removeEventListener('resize', handleResize);
          if (cb && cb.destroy) {
            cb.destroy();
          }

          if (_top) {
            $timeout.cancel(_top);
          }
        });
      }
    };
  }
];
