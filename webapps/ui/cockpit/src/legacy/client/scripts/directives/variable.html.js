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

module.exports = `<div>
  <!-- # CE - camunda-cockpit-ui/client/scripts/directives/variable.html -->

  <div ng-if="isBoolean(variable)">
    <label class="radio">
      <input ng-model="variable.value"
             ng-value="true"
             type="radio"
             name="booleanValue">
       true
    </label>

    <label class="radio">
      <input ng-model="variable.value"
             ng-value="false"
             type="radio"
             name="booleanValue">
       false
    </label>
  </div>

  <input ng-if="isInteger(variable) || isShort(variable) || isLong(variable)"
         class="form-control"
         name="editIntegerValue"
         type="text"
         numeric
         integer="true"
         ng-model="variable.value"
         ng-class="{'in-place-edit': isInPlaceEdit() }"
         autofocus="autofocus"
         required />

  <input ng-if="isDouble(variable) || isFloat(variable)"
         class="form-control"
         name="editFloatValue"
         type="text"
         numeric
         ng-model="variable.value"
         ng-class="{'in-place-edit': isInPlaceEdit() }"
         autofocus="autofocus"
         required />

  <span cam-widget-inline-field
        class="form-control-static"
        type="datetime"
        value="variable.value"
        ng-if="isDate(variable)"
        flexible="true"
        required>
           <a>{{variable.value || 'VARIABLE_SELECT_DATE' | translate}}</a>
  </span>

  <textarea ng-if="isString(variable) || isJSON(variable) || isXML(variable)"
            id="value"
            rows="5"
            ng-readonly="readonly"
            ng-model="variable.value"
            ng-change="changeVariableValue()"
            class="form-control variable-type-string"
            ng-class="{'in-place-edit': isInPlaceEdit() }"
            autofocus="autofocus"
            required></textarea>

  <div ng-if="isObject(variable)">
      <label class="control-label"
             for="objectTypeName">{{ 'VARIABLE_OBJECT_TYPE_NAME' | translate }}</label>
        <input id="objectTypeName"
               name="objectTypeName"
               class="form-control"
               type="text"
               ng-model="variable.valueInfo.objectTypeName"
               autofocus
               required />
      <label class="control-label"
             for="serializationDataFormat">{{ 'VARIABLE_SERIALIZATION_DATA_FORMAT' | translate }}</label>
        <input id="serializationDataFormat"
               name="serializationDataFormat"
               class="form-control"
               type="text"
               ng-model="variable.valueInfo.serializationDataFormat"
               required />
      <label class="control-label"
             for="serializedValue">{{ 'VARIABLE_SERIALIZED_VALUE' | translate }}</label>
        <textarea rows="5"
                  ng-model="variable.value"
                  class="form-control"
                  ></textarea>
  </div>
  <!-- / CE - camunda-cockpit-ui/client/scripts/directives/variable.html -->
</div>
`;
