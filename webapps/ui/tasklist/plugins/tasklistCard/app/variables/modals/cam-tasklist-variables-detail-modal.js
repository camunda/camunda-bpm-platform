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

module.exports = [
  '$scope',
  '$http',
  'Uri',
  'details',
  function($scope, $http, Uri, details) {
    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.value = null;
    $scope.valueDeserialized = null;
    $scope.deserializationError = null;
    $scope.type = null;
    $scope.dataFormat = null;
    $scope.variable = details;
    $scope.selectedTab = 'serialized';

    switch ($scope.variable.type) {
      case 'Object':
        $scope.type = $scope.variable.valueInfo.objectTypeName;
        $scope.value = $scope.variable.value;
        $scope.dataFormat = $scope.variable.valueInfo.serializationDataFormat;

        // attempt fetching the deserialized value
        $http({
          method: 'GET',
          url: Uri.appUri(
            'engine://engine/:engine' + $scope.variable._links.self.href
          )
        })
          .then(function(response) {
            $scope.valueDeserialized = JSON.stringify(response.data.value);
          })
          .catch(function(response) {
            $scope.deserializationError = response.data.message;
          });

        break;

      default:
        $scope.value = $scope.variable.value;
    }

    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
    };
  }
];
