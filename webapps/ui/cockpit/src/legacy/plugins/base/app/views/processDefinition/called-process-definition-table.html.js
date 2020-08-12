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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processDefinition/called-process-definition-table.html -->
<div cam-widget-loader
     loading-state="{{ loadingState }}"
     text-empty="{{'PLUGIN_CALLED_PROCESS_EMPTY' | translate}}">
  <table class="called-process-definition cam-table">
    <thead sortable-table-head
           head-columns="headColumns"
           on-sort-change="onSortChange(sortObj)"
           default-sort="sortObj">
    </thead>
    <tbody>
      <tr ng-repeat="calledProcessDefinition in calledProcessDefinitions | orderBy:sortObj.sortBy:sortObj.sortReverse">
        <td class="process-definition"
            cam-widget-clipboard="calledProcessDefinition.id">
          <a ng-href="#/process-definition/{{ calledProcessDefinition.id }}/runtime?parentProcessDefinitionId={{ processDefinition.id }}">
            {{ calledProcessDefinition.label }}
          </a>
        </td>

        <td class="activity">
          <span ng-show="calledProcessDefinition.calledFromActivities.length === 1"
                cam-widget-clipboard="calledProcessDefinition.calledFromActivities[0].id">
            <a ng-href="#/process-definition/{{ processDefinition.id }}/runtime?{{ getSearchQueryForSearchType(calledProcessDefinition.calledFromActivities[0].id) }}&amp;detailsTab=call-process-definitions-table">
              {{ calledProcessDefinition.calledFromActivities[0].name }}
            </a>
          </span>

          <ul ng-show="calledProcessDefinition.calledFromActivities.length > 1">
            <li ng-repeat="activity in calledProcessDefinition.calledFromActivities"
                cam-widget-clipboard="activity.id">
              <a ng-href="#/process-definition/{{ processDefinition.id }}/runtime?{{ getSearchQueryForSearchType(activity.id) }}&amp;detailsTab=call-process-definitions-table">
                {{ activity.name }}
              </a>
            </li>
          <ul>
        </td>
      </tr>
    </tbody>
  </table>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processDefinition/called-process-definition-table.html -->
`;
