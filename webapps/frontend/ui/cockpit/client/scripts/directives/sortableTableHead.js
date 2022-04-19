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

var template = require('./sortable-table-head.html')();

var Directive = function() {
  return {
    replace: false,
    restrict: 'AE',
    scope: {
      headColumns: '=?',
      onSortChange: '&',
      defaultSort: '=?'
    },
    template: template,
    controller: [
      '$scope',
      function($scope) {
        // Order Icons
        $scope.orderClass = function(forColumn) {
          forColumn = forColumn || $scope.defaultSort.sortBy;
          var icons = {
            none: 'minus',
            desc: 'chevron-down',
            asc: 'chevron-up'
          };
          return (
            'glyphicon-' +
            icons[
              forColumn === $scope.defaultSort.sortBy
                ? $scope.defaultSort.sortOrder
                : 'none'
            ]
          );
        };

        // On-click function to order Columns
        $scope.changeOrder = function(column) {
          $scope.defaultSort.sortBy = column;
          $scope.defaultSort.sortOrder =
            $scope.defaultSort.sortOrder === 'desc' ? 'asc' : 'desc';
          $scope.onSortChange({sortObj: $scope.defaultSort});
        };

        $scope.evaluateCondition = function(condition) {
          return typeof condition === 'undefined' ? true : condition;
        };

        $scope.formatClassName = function(className) {
          return className.replace(/\s/g, '-');
        };
      }
    ],
    link: function() {}
  };
};

module.exports = Directive;
