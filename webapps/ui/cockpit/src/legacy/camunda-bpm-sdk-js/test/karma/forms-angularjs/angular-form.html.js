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

module.exports = `<form cam-form role="form" name="form">

  <input cam-variable-name="stringVar"
    cam-variable-type="String"
    type="text"
    name="stringVar"
    ng-model="modelProperty"
    min-length="5"
    max-length="10">

  <input cam-variable-name="autoBindVar"
    cam-variable-type="String"
    type="text">

  <p>
  {{modelProperty}}
  </p>

  <input cam-variable-name="integerVar"
    cam-variable-type="Integer"
    type="text"
    name="integerVar"
    ng-model="integerProperty">

  <script cam-script type="text/form-script">

    // scope must be defined
    if(typeof $scope !== 'object') {
      throw Error('Expecting "$scope" to be defined');
    }

    // inject must be available
    if(typeof inject !== 'function') {
      throw Error('Expecting "inject" to be defined');
    }

    camForm.on('variables-applied', function() {
      if(!$scope.modelProperty) {
        throw Error('Expecting "$scope.modelProperty" to be defined');
      }
    });

  </script>

</form>

`;
