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
  '$location',
  'formData',
  ($scope, $location, formData) => {
    $scope.formData = {...formData}; // don't bind textarea to model of text input field

    $scope.changeValue = () => {
      formData.editValue = $scope.formData.editValue
        .split('\n')
        // trim whitespace and remove ',' at the start and end of lines
        .map(line => line.replace(/^\s*,+\s*|\s*,+\s*$/g, ''))
        .join(','); // allow line break as separator
      $scope.$dismiss();
    };
  }
];
