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
var $ = require('jquery');

var template = require('./../modals/cam-tasklist-filter-modal.html')();

module.exports = [
  '$scope',
  '$uibModal',
  '$q',
  'camAPI',
  '$timeout',
  function($scope, $modal, $q, camAPI, $timeout) {
    var filtersData = ($scope.filtersData = $scope.tasklistData.newChild(
      $scope
    ));

    var Filter = camAPI.resource('filter');

    $scope.userCanCreateFilter = false;

    // provide /////////////////////////////////////////////////////////////////////////////////

    filtersData.provide('filterAuthorizations', function() {
      var deferred = $q.defer();

      Filter.authorizations(function(err, res) {
        if (err) {
          deferred.reject(err);
        } else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    });

    filtersData.provide('userCanCreateFilter', [
      'filterAuthorizations',
      function(filterAuthorizations) {
        filterAuthorizations = filterAuthorizations || {};
        var links = filterAuthorizations.links || [];

        for (var i = 0, link; (link = links[i]); i++) {
          if (link.rel === 'create') {
            return true;
          }
        }

        return false;
      }
    ]);

    // observe ////////////////////////////////////////////////////////////////////////////////

    filtersData.observe('userCanCreateFilter', function(userCanCreateFilter) {
      $scope.userCanCreateFilter = userCanCreateFilter;
    });

    var doAfterFilterUpdate = [];
    filtersData.observe('filters', function(filters) {
      doAfterFilterUpdate.forEach(function(fct) {
        fct(filters);
      });
      doAfterFilterUpdate = [];
    });

    // open modal /////////////////////////////////////////////////////////////////////////////

    var focusFilter = function(filter) {
      if (filter) {
        doAfterFilterUpdate.push(function() {
          $timeout(function() {
            var element = $(
              '.task-filters .content div.item.active .actions a'
            )[0];
            element && element.focus();
          });
        });
      } else {
        document.querySelector('.task-filters header button.btn-link').focus();
      }
    };

    $scope.$on('shortcut:focusFilter', function() {
      $('.task-filters .content h4 a')[0].focus();
    });

    $scope.openModal = function($event, filter) {
      $event.stopPropagation();

      var handleDialogClose = function() {
        $timeout(function() {
          filtersData.changed('filters');
          focusFilter(filter);
        }, 20);
      };

      $modal
        .open({
          windowClass: 'filter-modal',
          size: 'lg',
          controller: 'camFilterModalCtrl',
          template: template,
          resolve: {
            filter: function() {
              return filter;
            },
            filtersData: function() {
              return filtersData;
            }
          }
        })
        .result.then(handleDialogClose, handleDialogClose);
    };
  }
];
