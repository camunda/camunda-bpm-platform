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

var angular = require('../../../../../camunda-bpm-sdk-js/vendor/angular'),
    headerDefinition = require('../cam-widget-header');

require('angular-ui-bootstrap');
require('angular-translate');

// naive auth object for test purposes
function Authentication(username, accesses) {
  this.name = username || '';
  this.authorizedApps = accesses || [];
}
Authentication.prototype.canAccess = function(appName) {
  return !this.authorizedApps.length || this.authorizedApps.indexOf(appName) > -1;
};


var mockedDependenciesModule = angular.module('mockedDependencies', []);

mockedDependenciesModule.service('AuthenticationService', [function() {
  this.logout = function() {
    // logging out
  };
}]);

mockedDependenciesModule.provider('uriFilter', [function() {
  return { $get: function() {} };
}]);
mockedDependenciesModule.filter('uri', [function() {
  return function() { return '#uri-filter-replaced'; };
}]);



var headerModule = angular.module('headerModule', [
  'ui.bootstrap',
  mockedDependenciesModule.name
]);
headerModule.directive('camWidgetHeader', headerDefinition);



var testModule = angular.module('testModule', [headerModule.name]);

testModule.controller('testAnonymousController', ['$scope', function($scope) {
  $scope.ctrlCurrentApp = 'admin';
  $scope.auth = new Authentication();
}]);

testModule.controller('testAuthenticatedController', ['$scope', '$timeout', function($scope, $timeout) {
  $scope.ctrlCurrentApp = 'tasklist';

  $scope.auth = new Authentication('mustermann', ['tasklist', 'admin']);

  $timeout(function() {
    $scope.fullName = 'Max Mustermann';
  }, 400);
}]);

testModule.controller('testAuthenticatedSingleController', ['$scope', '$timeout', function($scope, $timeout) {
  $scope.auth = new Authentication('mustermann', ['tasklist']);

  $timeout(function() {
    $scope.fullName = 'Max Mustermann';
  }, 400);
}]);

angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name, 'pascalprecht.translate']);
});
