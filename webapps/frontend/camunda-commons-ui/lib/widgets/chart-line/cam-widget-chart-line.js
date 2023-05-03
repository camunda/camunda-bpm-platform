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

var throttle = require('lodash').throttle;
var LineChart = require('./../../chart/line');
var moment = require('camunda-bpm-sdk-js/vendor/moment'); // this should be left as-is to support development with `line.html`
var abbreviateNumber = require('./../../filter/abbreviateNumber')();

module.exports = [
  '$window',
  function($window) {
    return {
      restrict: 'A',

      scope: {
        fontFamily: '=?',
        fontSize: '=?',
        handleColor: '=?',
        handleColorHover: '=?',
        handleWidth: '=?',
        interval: '=?',
        lineColors: '=?',
        lineWidth: '=?',
        rulersColor: '=?',
        selectingColor: '=?',
        selection: '&onSelection',
        textPadding: '=?',
        tickSize: '=?',
        timeLabelFormats: '=?',
        timespan: '=?',
        timestampFormat: '=?',
        unselectedColor: '=?',
        valueLabelsCount: '=?',
        values: '=',
        disableSelection: '=',
        isLogScale: '='
      },

      link: function($scope, $element) {
        var container = $element[0];
        var computedStyles = $window.getComputedStyle(container);

        $scope.timespan = $scope.timespan || 'day';
        $scope.interval = $scope.interval || 900;

        function height() {
          return Math.min(Math.max(container.clientWidth * 0.75, 180), 300);
        }

        var chart = ($scope.chart = new LineChart({
          moment: moment,
          abbreviateNumber: abbreviateNumber,

          onselection: function onselection(info) {
            $scope.$apply(function() {
              $scope.selection({
                info: info
              });
            });
          },

          width: container.clientWidth,
          height: height(),

          fontFamily: $scope.fontFamily || computedStyles.fontFamily,
          fontSize: $scope.fontSize,
          handleColor: $scope.handleColor,
          handleColorHover: $scope.handleColorHover,
          handleWidth: $scope.handleWidth,
          lineColors: $scope.lineColors,
          lineWidth: $scope.lineWidth,
          rulersColor: $scope.rulersColor || computedStyles.color,
          selectingColor: $scope.selectingColor,
          textPadding: $scope.textPadding,
          tickSize: $scope.tickSize,
          timeLabelFormats: $scope.timeLabelFormats,
          timestampFormat: $scope.timestampFormat,
          unselectedColor: $scope.unselectedColor,
          valueLabelsCount: $scope.valueLabelsCount,
          disableSelection: $scope.disableSelection,
          isLogScale: $scope.isLogScale
        }));

        $scope.$watch('values', function() {
          var cn = container.className.replace('no-data', '');
          if (
            !$scope.values ||
            !$scope.values.length ||
            !$scope.values[0] ||
            !$scope.values[0].length
          ) {
            cn += ' no-data';
            chart.setData([[]], $scope.timespan, $scope.interval);
          } else {
            chart.setData($scope.values, $scope.timespan, $scope.interval);
          }
          container.className = cn;
        });

        container.appendChild(chart.canvas);

        var resize = throttle(function() {
          chart.resize(container.clientWidth, height()).draw();
        }, 100);

        $window.addEventListener('resize', resize);

        $scope.$on('$destroy', function() {
          $window.removeEventListener('resize', resize);
        });
      },

      template: '<!-- keule!! pech jehabt! -->'
    };
  }
];
