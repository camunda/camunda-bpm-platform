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

module.exports = `<div ng-if="display && isShown('type')"
     class="type">{{ variable.type }}</div>
<div ng-if="display && isShown('name')"
     class="name">{{ variable.name }}</div>
<div ng-if="display && isShown('value') && isPrimitive()"
     ng-class="{null: isNull()}"
     class="value">
  <span ng-if="isNull()"
        class="null-symbol">{{ 'CAM_WIDGET_VARIABLE_NULL' | translate }}</span>
  {{ (variable.value === null ? '' : variable.value.toString()) }}
</div>
<div ng-if="display && isShown('value') && variable.type === 'Object'"
     ng-class="{null: isNull()}"
     class="value">
  <a ng-click="editVariableValue()">
    {{ variable.valueInfo.objectTypeName }}
  </a>
</div>


<div ng-if="!display"
     class="input-group editing">
  <div ng-if="isShown('type')"
       class="input-group-btn type">
    <select class="form-control"
            ng-model="variable.type"
            ng-options="variableType for variableType in variableTypes track by variableType"
            ng-disabled="isDisabled('type')"
            required>
    </select>
  </div><!-- /btn-group -->

  <input ng-if="isShown('name')"
         type="text"
         class="form-control name"
         ng-model="variable.name"
         placeholder="varName"
         ng-disabled="isDisabled('name')"
         required />

  <div ng-if="isShown('value') && !isNull()"
       class="value-wrapper input-group"
       ng-class="{checkbox: useCheckbox()}">
    <div ng-if="variable.type !== 'Null'"
         class="input-group-btn">
      <a ng-click="setNull()"
         class="btn btn-default set-null"
         ng-disabled="isDisabled('value')"
         uib-tooltip="{{ 'CAM_WIDGET_VARIABLE_SET_VALUE_NULL' | translate }}">
        <span class="glyphicon glyphicon-remove"></span>
      </a>
    </div>

    <input ng-if="isPrimitive() && !useCheckbox()"
           type="text"
           class="form-control value"
           ng-model="variable.value"
           ng-disabled="isDisabled('value')"
           placeholder="{{ 'CAM_WIDGET_VARIABLE_VALUE' | translate }}"
           cam-variable-validator="{{variable.type}}" />

    <input ng-if="useCheckbox()"
           type="checkbox"
           class="value"
           ng-model="variable.value"
           ng-disabled="isDisabled('value')"
           placeholder="{{ 'CAM_WIDGET_VARIABLE_VALUE' | translate }}"
           cam-variable-validator="{{variable.type}}" />

    <div ng-if="variable.type === 'Object'"
         class="value form-control-static">
      <a ng-click="editVariableValue()" ng-disabled="isDisabled('value')">
        {{ (variable.valueInfo.objectTypeName || 'CAM_WIDGET_VARIABLE_UNDEFINED') | translate }}
      </a>
    </div>
  </div>

  <div ng-if="variable.type !== 'Null' && isShown('value') && isNull()"
       ng-click="setNonNull()"
       class="value-wrapper value null-value btn btn-default"
       ng-disabled="isDisabled('value')"
       uib-tooltip="{{ 'CAM_WIDGET_VARIABLE_RESET' | translate }}">
    <span class="null-symbol">{{ 'CAM_WIDGET_VARIABLE_NULL' | translate }}</span>
  </div>

  <div ng-if="variable.type === 'Null'"
       ng-disabled="isDisabled('value')"
       class="value-wrapper value btn no-click null-value">
    <span class="null-symbol">{{ 'CAM_WIDGET_VARIABLE_NULL' | translate }}</span>
  </div>
</div>
`;
