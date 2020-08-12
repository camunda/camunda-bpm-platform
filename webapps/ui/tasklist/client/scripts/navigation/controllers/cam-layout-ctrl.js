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

var $ = require('jquery');
var $bdy = $('body');

module.exports = [
  '$scope',
  '$timeout',
  function($scope, $timeout) {
    $scope.toggleVariableSearch = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      $('.tasks-list').toggleClass('show-search');
    };

    function region($event) {
      return $($event.currentTarget).attr('data-region');
    }

    function isClosed(target) {
      return $bdy.hasClass(target + '-column-close');
    }

    function open(target) {
      return $bdy.removeClass(target + '-column-close');
    }

    function close(target) {
      return $bdy.addClass(target + '-column-close');
    }

    $scope.toggleRegion = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      var target = region($event);

      // list-column-close is not allowed when task-column-close
      if (target === 'task') {
        if (isClosed('list') && !isClosed('task')) {
          open('list');
        }
      } else if (target === 'list') {
        if (isClosed('task') && !isClosed('list')) {
          open('task');
        }
      }

      $bdy.toggleClass(target + '-column-close');
      $timeout(function() {
        $scope.$root.$broadcast('layout:change');
      }, 600);
    };

    $scope.maximizeRegion = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      close('filters');
      close('list');
      open('task');
      document.querySelector('.reset-regions').focus();
    };

    $scope.resetRegions = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      open('filters');
      open('list');
      open('task');
      document.querySelector('.maximize').focus();
    };
  }
];
