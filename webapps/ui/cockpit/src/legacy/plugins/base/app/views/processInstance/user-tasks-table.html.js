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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/user-tasks-table.html -->
<div cam-widget-loader
     loading-state="{{ loadingState }}"
     text-empty="{{ 'PLUGIN_USER_TASKS_NO_USER_TASKS' | translate }}">
  <table class="process-instance user-tasks cam-table">
    <thead sortable-table-head
           head-columns="headColumns"
           on-sort-change="onSortChange(sortObj)"
           default-sort="sorting">
    </thead>

    <tbody>
      <tr ng-repeat="userTask in userTasks">
        <td class="activity">
          <a ng-href="{{ getHref(userTask) }}">{{ userTask.instance.name || userTask.instance.id }}</a>
        </td>

        <td class="assignee">
          <cam-in-place-text-field
            submit="submitAssigneeChange"
            context="userTask"
            property="assignee" />
        </td>

        <td class="owner">
          {{ userTask.owner }}
        </td>

        <td class="created">
          {{ userTask.created | camDate }}
        </td>
        <td class="due">
          {{ userTask.due | camDate }}
        </td>
        <td class="follow-up">
          {{ userTask.followUp | camDate }}
        </td>
        <td class="priority">
          {{ userTask.priority }}
        </td>
        <td class="delegation-state">
          {{ userTask.delegationState }}
        </td>
        <td class="task-id uuid"
            cam-widget-clipboard="userTask.id">
          <a ng-href="{{ getTasklistHref(userTask) }}"
             uib-tooltip="{{ 'PLUGIN_USER_TASKS_LINK_TO_TASKLIST' | translate }}">{{ userTask.id }}</a>
        </td>

        <td class="action">
          <a ng-click="changeGroupIdentityLinks()"
             ng-model="userTask"
             class="btn btn-default action-button change-group-identity-links"
             tooltip-placement="left"
             uib-tooltip="{{ translate('PLUGIN_USER_TASKS_MANAGE_GROUP_TOOLTIP', { value: userTask.name || userTask.id }) }}">
            <span class="glyphicon glyphicon-th"></span>
          </a>
          <a ng-click="changeUserIdentityLinks()"
             ng-model="userTask"
             class="btn btn-default action-button change-user-identity-links"
             tooltip-placement="left"
             uib-tooltip="{{ translate('PLUGIN_USER_TASKS_MANAGE_USER_TOOLTIP', { value: userTask.name || userTask.id }) }}">
            <span class="glyphicon glyphicon-user"></span>
          </a>
        </td>
      </tr>
    </tbody>
  </table>


  <ul uib-pagination ng-if="pages.total > pages.size"
              class="pagination-sm"

              page="pages.current"
              ng-model="pages.current"

              total-items="pages.total"
              items-per-page="pages.size"

              max-size="7"
              boundary-links="true"></ul>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/user-tasks-table.html -->
`;
