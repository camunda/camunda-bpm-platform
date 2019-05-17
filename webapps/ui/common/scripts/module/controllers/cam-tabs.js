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

module.exports = ['$scope', 'search', 'Views', CamTabs];

function CamTabs($scope, search, Views) {
  this.providers = this.getProviders(Views, $scope);
  this.selected = this.providers[0];
  this.search = search;

  this.initializeVars($scope);

  $scope.$on('$locationChangeSuccess', this.onLocationChange.bind(this));
  this.onLocationChange();
}

CamTabs.prototype.initializeVars = function($scope) {
  this.vars = $scope.vars || {
    read: ['tabsApi']
  };

  if ($scope.varsValues) {
    angular.extend($scope, $scope.varsValues);
  }
};

CamTabs.prototype.getProviders = function(Views, $scope) {
  return Views.getProviders($scope.providerParams).sort(compareProviders);
};

function compareProviders(providerA, providerB) {
  return (providerB.priority || 0) - (providerA.priority || 0);
}

CamTabs.prototype.onLocationChange = function() {
  var params = this.search();

  if (this.isTabSelectionChangedInUrl(params)) {
    this.selected = this.providers.filter(function(provider) {
      return provider.id === params.tab;
    })[0];
  } else if (!params.tab) {
    this.selected = this.providers[0];
  }
};

CamTabs.prototype.isTabSelectionChangedInUrl = function(params) {
  return (
    angular.isString(params.tab) &&
    (!this.selected || params.tab !== this.selected.id)
  );
};

CamTabs.prototype.selectTab = function(tabProvider) {
  var params = this.search();
  var tabParams = {
    tab: tabProvider.id
  };

  this.selected = tabProvider;

  this.search.updateSilently(angular.extend(params, tabParams));
};

CamTabs.prototype.isSelected = function(tabProvider) {
  return this.selected === tabProvider;
};
