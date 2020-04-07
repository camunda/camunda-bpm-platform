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

module.exports = `<form cam-form role="form">

  <input cam-variable-name="stringVar"
         cam-variable-type="String"
         type="text"
         class="form-control"
         id="stringVar">

  <input type="text"
         class="form-control"
         id="customField">

  <script cam-script type="text/form-script">

    var variableManager = camForm.variableManager;
    var formElement = camForm.formElement;

    camForm.on('form-loaded', function() {

      // declare a new variable
      variableManager.createVariable({
        'name': 'customVar',
        'type': 'String',
        'value': 'someValue'
      });

    });

    camForm.on('variables-fetched', function() {

      $('#customField', formElement)
        .val(variableManager.variableValue('customVar'));

    });

    camForm.on('submit', function(evt) {

      // prevent submit if value of form field was not changed
      if($('#customField', formElement).val() == 'someValue') {
        evt.submitPrevented = true;
      }

    });

  </script>

</form>
`;
