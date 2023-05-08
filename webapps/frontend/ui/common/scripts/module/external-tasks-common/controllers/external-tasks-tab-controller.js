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

module.exports = [
  '$scope',
  'exposeScopeProperties',
  'search',
  ExternalTasksTabController
];

module.exports.ExternalTasksTabController = ExternalTasksTabController;

function ExternalTasksTabController($scope, exposeScopeProperties, search) {
  exposeScopeProperties($scope, this, ['onLoad']);

  // reset Page when changing Tabs
  $scope.$on('$destroy', function() {
    search('page', null);
  });

  var processData = $scope.processData.newChild($scope);

  processData.observe('filter', this.onFilterChanged.bind(this));
}

ExternalTasksTabController.prototype.onFilterChanged = function(filter) {
  if (this.isFilterChanged(filter)) {
    this.filter = filter;

    if (this.pages) {
      this.loadTasks();
    }
  }
};

ExternalTasksTabController.prototype.isFilterChanged = function(filter) {
  var lastActivities = getActivityIdsFromFilter(this.filter);
  var currentActivities = getActivityIdsFromFilter(filter);

  return !this.filter || !angular.equals(lastActivities, currentActivities);
};

ExternalTasksTabController.prototype.onPaginationChange = function(pages) {
  this.pages = pages;

  if (this.filter) {
    this.loadTasks();
  }
};

ExternalTasksTabController.prototype.loadTasks = function() {
  this.loadingState = 'LOADING';

  this.onLoad({
    pages: angular.copy(this.pages), //just a defensive copy
    activityIds: getActivityIdsFromFilter(this.filter)
  }).then(
    function(data) {
      this.total = data.count;

      if (!data.list) {
        this.loadingState = 'EMPTY';
      } else {
        this.loadingState = 'LOADED';
      }
    }.bind(this)
  );
};

function getActivityIdsFromFilter(filter) {
  if (!filter || !filter.activityIds || !filter.activityIds.length) {
    return null;
  }

  return filter.activityIds;
}
