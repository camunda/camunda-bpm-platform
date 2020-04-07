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

module.exports = `<!-- # CE - camunda-commons-ui/lib/directives/inPlaceTextField.html -->
<div in-place-text-field-root>
  <div ng-if="!editing">
    {{ context[property] }}
    <span class="edit-toggle"
          ng-click="enter()">
      <span class="glyphicon glyphicon-pencil"></span>
    </span>
  </div>

  <form ng-if="editing"
        ng-submit="submit()"
        class="inline-edit"
        name="inPlaceTextFieldForm"
        novalidate
        request-aware>

    <fieldset>
      <!-- {{ value }} -->
      <input name="value"
             ng-model="value"
             type="text"
             class="in-place-edit form-control"
             placeholder="{{ placeholder }}"
             autofocus
             ng-required="isRequired">
    </fieldset>

    <div class="inline-edit-footer">

      <p class="error" ng-show="error">
        {{ error.message }}
      </p>

      <div class="btn-group">
        <button type="submit"
                class="btn btn-sm btn-primary"
                ng-disabled="inPlaceTextFieldForm.$invalid">
          <span class="glyphicon glyphicon-ok "></span>
        </button>
        <button type="button"
                class="btn btn-sm btn-default"
                ng-click="leave()">
          <span class="glyphicon glyphicon-ban-circle"></span>
        </button>
      </div>
    </div>

  </form>
</div>
<!-- / CE - camunda-commons-ui/lib/directives/inPlaceTextField.html -->
`;
