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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/jobs-tab.html -->

<div cam-widget-loader
loading-state="{{ loadingState }}"
text-empty="{{ 'PLUGIN_JOBS_NO_JOBS' | translate }}">

  <table class="jobs-tab cam-table">

    <thead sortable-table-head
      head-columns="headColumns"
      on-sort-change="onSortChange(sortObj)"
      default-sort="sorting">
    </thead>

    <tbody>
      <tr ng-repeat="job in jobs">
        <td class="id">
            <span cam-widget-clipboard="job.id">{{job.id}}</span>
        </td>
        <td class="date">
            <span ng-if="job.dueDate" cam-widget-clipboard="job.dueDate | camDate">{{job.dueDate | camDate}}</span>
            <span ng-if="!job.dueDate" class="null-value">null</span>
        </td>
        <td>
            <span cam-widget-clipboard="job.createTime | camDate">{{job.createTime | camDate}}</span>
        </td>
        <td>
          {{job.retries}}
        </td>
        <td>
          <a ng-href="#/process-instance/{{ processInstance.id }}/runtime?activityIds={{job.activityId}}&amp;tab=jobs-tab">
            {{job.activityName}}
          </a>
        </td>
        <td>
          <span ng-if="job.failedActivityId"
          cam-widget-clipboard="job.failedActivityId">
            {{ job.failedActivityName }}
          </span>
        </td>
        <td>
            <a class="btn btn-sm btn-default action-button"
               ng-click="toggleSuspension(job)"
               ng-if="job.suspended"
               uib-tooltip="{{ 'PLUGIN_JOBS_RESUME_TOOLTIP' | translate }}"
               tooltip-placement="left">

              <span class="glyphicon glyphicon-play"></span>
            </a>
            <a class="btn btn-sm btn-default action-button"
               ng-click="toggleSuspension(job)"
               ng-if="!job.suspended"
               uib-tooltip="{{ 'PLUGIN_JOBS_SUSPEND_TOOLTIP' | translate }}"
               tooltip-placement="left">

              <span class="glyphicon glyphicon-pause"></span>
            </a>
            <a class="btn btn-sm btn-default action-button"
               ng-click="openRecalculationWindow(job)"
               ng-if="job.dueDate"
               uib-tooltip="{{ 'PLUGIN_JOB_RECALCULATE_TOOLTIP' | translate }}"
               tooltip-placement="left">

              <span class="glyphicon glyphicon-time"></span>
            </a>
        </td>
      </tr>
    </tbody>
  </table>

  <div cam-pagination="onPaginationChange(pages)" total="pages.total"></div>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/jobs-tab.html -->`;
