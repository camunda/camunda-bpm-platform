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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processDefinition/process-instance-table.html -->
<div cam-searchable-area
     config="searchConfig"
     array-types="['activityIdIn']"
     variable-types="['variables']"
     on-search-change="onSearchChange(query, pages)"
     loading-state="loadingState"
     text-empty="{{'PLUGIN_PROCESS_INSTANCE_NO_PROCESS' | translate}}"
     storage-group="'PDI'">

  <table class="process-instances cam-table">

    <thead sortable-table-head
           head-columns="headColumns"
           on-sort-change="onSortChange(query, pages, sortObj)"
           default-sort="sortObj">
    </thead>

    <tbody>
      <tr ng-repeat="processInstance in processInstances">
        <td class="state">
          <a state-circle
             incidents="processInstance.incidents"
             ng-href="{{ getProcessInstanceUrl(processInstance, { tab: 'incidents-tab' }) }}"></a>
          <span class="badge badge-warning badge-suspended"
                ng-show="processInstance.suspended"
                uib-tooltip="{{'PLUGIN_PROCESS_INSTANCE_SUSPENDED' | translate}}"
                tooltip-placement="left">
            <span class="glyphicon glyphicon-pause white"></span>
          </span>
        </td>

        <td class="instance-id"
            cam-widget-clipboard="processInstance.id">
          <a ng-href="{{ getProcessInstanceUrl(processInstance) }}"
             title="{{ processInstance.id }}">
            {{ processInstance.id }}
          </a>
        </td>

        <td class="start-time"
            cam-widget-clipboard="processInstance.startTime | camDate">
          {{ processInstance.startTime | camDate }}
        </td>

        <td class="business-key"
            ng-if="processInstance.businessKey"
            cam-widget-clipboard="processInstance.businessKey">
          {{ processInstance.businessKey }}
        </td>
        <td class="business-key"
            ng-if="!processInstance.businessKey"></td>
      </tr>
    </tbody>
  </table>
</div>

<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processDefinition/process-instance-table.html -->
`;
