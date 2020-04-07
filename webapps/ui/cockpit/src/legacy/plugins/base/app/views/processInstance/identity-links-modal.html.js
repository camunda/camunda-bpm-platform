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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/identity-links-modal.html -->
<div class="modal-header">
  <h3 class="modal-title">{{ title | translate }}</h3>
</div>

<div class="identity-links modal-body">
  <div notifications-panel></div>

  <form name="editForm"
        ng-submit="!invalid() && addItem()">
    <fieldset ng-show="identityLinks.length">
      <legend>{{ decorator.table.label }}</legend>

      <table class="table cam-table">
        <thead>
          <tr>
            <th class="id">{{ decorator.table.id }}</th>
            <th class="action text-right">{{ 'PLUGIN_IDENTITY_LINKS_ACTION' | translate }}</th>
          </tr>
        </thead>

        <tbody>
          <tr ng-repeat="(delta, identityLink) in identityLinks">
            <td class="id">
              <span cam-widget-clipboard="identityLink[key]">{{ identityLink[key] }}</span>
            </td>
            <td class="action text-right">
              <span class="btn btn-default action-button"
                    ng-click="removeItem()">
                <span class="glyphicon glyphicon-ban-circle"></span>
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </fieldset>

    <fieldset>
      <legend>{{ decorator.add.label }}</legend>

      <div class="input-group">
        <input class="form-control"
               type="text"
               ng-model="newItem"
               name="newItem"
               required />
        <span class="btn btn-primary input-group-addon"
              ng-click="addItem()"
              ng-disabled="invalid()">
          <span class="glyphicon glyphicon-plus"></span>
        </span>
      </div>
    </fieldset>
  </form>
</div>

<div class="modal-footer">
  <button ng-click="$dismiss()"
          class="btn btn-default">
    {{ 'PLUGIN_IDENTITY_LINKS_CLOSE' | translate }}
  </button>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/identity-links-modal.html -->
`;
