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

var template = require('./cam-sortable-table-column.html')();

var Directive = function() {
  return {
    replace: false,
    restrict: 'A',
    scope: {
      column: '@sortByProperty'
    },
    transclude: true,
    require: '^^camSortableTableHeader',
    template: template,
    link: function($scope, element, attrs, ctrl) {
      // Order Icons
      $scope.orderClass = function(forColumn) {
        var sorting = ctrl.getSorting();
        forColumn = forColumn || sorting.sortBy;
        var icons = {
          none: 'minus',
          desc: 'chevron-down',
          asc: 'chevron-up'
        };
        return (
          'glyphicon-' +
          icons[forColumn === sorting.sortBy ? sorting.sortOrder : 'none']
        );
      };

      // On-click function to order Columns
      $scope.changeOrder = function(column) {
        ctrl.changeOrder(column);
      };
    }
  };
};

module.exports = Directive;
