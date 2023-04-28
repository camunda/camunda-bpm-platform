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
  '$location',
  'params',
  'searchWidgetUtils',
  ExternalTaskActivityLink
];

function ExternalTaskActivityLink(
  $scope,
  exposeScopeProperties,
  search,
  $location,
  params,
  searchWidgetUtils
) {
  exposeScopeProperties($scope, this, [
    'activityId',
    'bpmnElements',
    'searchQueryType'
  ]);

  this.search = search;
  this.params = params;
  this.path = $location.path();
  this.searchWidgetUtils = searchWidgetUtils;
}

ExternalTaskActivityLink.prototype.getLink = function() {
  //search returns object that when modified changes query parameters
  //which is not desired here, hence it is needed to actually copy this object.
  var params = angular.copy(this.search());
  var searchQuery = JSON.parse(params.searchQuery || '[]');

  if (this.searchQueryType) {
    params.searchQuery = JSON.stringify(
      this.searchWidgetUtils.replaceActivitiesInSearchQuery(
        searchQuery,
        this.searchQueryType,
        [this.activityId]
      )
    );
  } else {
    params.activityIds = this.activityId;
  }

  return '#' + this.path + '?' + this.params(params);
};

ExternalTaskActivityLink.prototype.getActivityName = function() {
  var activityId = this.activityId;

  if (this.bpmnElements[activityId] && this.bpmnElements[activityId].name) {
    return this.bpmnElements[activityId].name;
  }

  return activityId;
};
