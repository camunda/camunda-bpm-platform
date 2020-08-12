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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/jobDefinition/app/views/processDefinition/job-definition-table.html -->
<table class="job-definition cam-table">

  <thead sortable-table-head
         head-columns="headColumns"
         on-sort-change="onSortChange(sortObj)"
         default-sort="sortObj">
  </thead>
  <tbody>
    <tr ng-repeat="jobDefinition in jobDefinitions | orderBy:sortObj.sortBy:sortObj.sortReverse">
      <td class="state">
        <span ng-show="jobDefinition.suspended">
          {{ 'PLUGIN_JOBDEFINITION_SUSPENDED' | translate }}
        </span>
        <span ng-hide="jobDefinition.suspended">
          {{ 'PLUGIN_JOBDEFINITION_ACTIVE' | translate }}
        </span>
      </td>
      <td class="activity"
          cam-widget-clipboard="jobDefinition.activityId">
        <a ng-href="#/process-definition/{{ processDefinition.id }}/runtime?{{ getSearchQueryForSearchType(jobDefinition.activityId) }}&amp;detailsTab=job-definition-table">
          {{ jobDefinition.activityName }}
        </a>
      </td>
      <td class="type"
          cam-widget-clipboard="jobDefinition.jobType">
        {{ jobDefinition.jobType }}
      </td>
      <td class="configuration">
        {{ jobDefinition.jobConfiguration }}
      </td>
      <td class="overriding-job-priority">
        {{ jobDefinition.overridingJobPriority }}
      </td>
      <td class="action">
        <span ng-repeat="actionProvider in jobDefinitionActions">
          <view provider="actionProvider" vars="jobDefinitionVars"/>
        </span>
      </td>
    </tr>

    <tr ng-if="!jobDefinitions">
      <td colspan="6">
        <span class="glyphicon glyphicon-loading"></span>
        {{ 'PLUGIN_JOBDEFINITION_LOADING_JOB' | translate }}
      </td>
    </tr>

    <tr ng-if="jobDefinitions && !jobDefinitions.length">
      <td colspan="6">
        {{ 'PLUGIN_JOBDEFINITION_NO_JOB_DEFINITION' | translate }}
      </td>
    </tr>

  </tbody>
</table>
<ul uib-pagination ng-if="pages.total > pages.size && jobDefinitions && jobDefinitions.length"
    class="pagination-sm"

    page="pages.current"
    ng-model="pages.current"

    total-items="pages.total"
    items-per-page="pages.size"

    max-size="7"
    boundary-links="true"></ul>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/jobDefinition/app/views/processDefinition/job-definition-table.html -->
`;
