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

module.exports = `<form name="changeVersionForm" ng-if="isActive" class="change-version" ng-class="{'has-error': !isValid}">
  <input type="text"
         class="form-control"
         name="defVersion"
         ng-keydown="enableValidating()"
         ng-model="model.newVersion"
         ng-change="change(changeVersionForm)"
         ng-model-options="{debounce: 300}"
         required>
         <button ng-click="changeLocation()"
          ng-disabled="!isValid || isValidating || storedVersion == model.newVersion"
          class="btn btn-xs btn-default">
    <span class="glyphicon glyphicon-ok"></span>
  </button>
  <button ng-click="close(changeVersionForm)"
          class="btn btn-xs btn-default">
    <span class="glyphicon glyphicon-remove"></span>
  </button>
</form>
<button ng-if="!isActive"
        ng-click="open()"
        class="btn btn-xs btn-default">
  <span>{{ definition.version }}</span>
</button>
`;
