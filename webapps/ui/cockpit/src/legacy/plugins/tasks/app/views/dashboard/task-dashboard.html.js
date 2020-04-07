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

module.exports = `<!-- # CE - ui/cockpit/plugins/tasks/app/views/dashboard/task-dashboard.html -->
<div class="dashboard-row">
  <section class="col-xs-12 col-md-6">
    <div class="inner">
      <header>
        <h1 class="section-title">{{ 'PLUGIN_TASK_ASSIGNMENT_BY_TYPE' | translate }}</h1>
      </header>
      <table class="cam-table values-left" id="open-task-statistics">
        <thead>
          <tr>
            <th>{{ 'PLUGIN_TASK_TASKS' | translate }}</th>
            <th>{{ 'PLUGIN_TASK_TYPES' | translate }}</th>
          </tr>
        </thead>

        <tfoot>
          <tr>
            <th>
              <span ng-if="!openTasksState.$loaded" class="glyphicon glyphicon-refresh animate-spin"></span>
              <span ng-if="openTasksState.$loaded && !hasSearchPlugin">{{ openTasksCount }}</span>
              <span ng-if="openTasksState.$loaded && hasSearchPlugin">
                <a class="search-link" ng-click="createSearch('allOpenTasks', 'statistics')">{{ openTasksCount }}</a>
              </span>
            </th>
            <th>{{ 'PLUGIN_TASK_TOTAL' | translate }}</th>
          </tr>
        </tfoot>

        <tbody>
          <tr ng-repeat="taskStatistic in taskStatistics">
            <th>
              <span ng-if="!taskStatistic.state.$loaded" class="glyphicon glyphicon-refresh animate-spin"></span>
              <span ng-if="taskStatistic.state.$loaded && !hasSearchPlugin">{{ taskStatistic.count }}</span>
              <span ng-if="taskStatistic.state.$loaded && hasSearchPlugin">
                <a class="search-link" ng-click="createSearch(taskStatistic.search, 'statistics')"> {{ taskStatistic.count }}</a>
              </span>
            </th>
            <td>{{ taskStatistic.label }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <section class="col-xs-12 col-md-6">
    <div class="inner">
      <header>
        <h1 class="section-title">{{ 'PLUGIN_TASK_ASSIGNMENT_BY_GROUP' | translate }}</h1>
      </header>
      <table class="cam-table values-left" id="task-group-counts">
        <thead>
          <tr>
            <th>{{ 'PLUGIN_TASK_TASKS' | translate }}</th>
            <th>{{ 'PLUGIN_TASK_GROUP' | translate }}</th>
          </tr>
        </thead>

        <tbody>
          <tr ng-if="!taskGroupState.$loaded && !taskGroupState.$error">
            <td colspan="2"><span class="glyphicon glyphicon-refresh animate-spin"></span></td>
          </tr>
          <tr ng-if="taskGroupState.$loaded" ng-repeat="taskGroup in taskGroups">
            <td ng-if="!hasSearchPlugin">{{ taskGroup.taskCount }}</td>
            <td ng-if="hasSearchPlugin">
              <a class="search-link" ng-click="createSearch(taskGroup.id, 'group')">{{ taskGroup.taskCount }}</a>
            </td>
            <td>{{ formatGroupName(taskGroup.groupName) }}</td>
          </tr>
        </tbody>
      </table>
      <p id="multiple-groups-info" ng-if="taskGroupState.$loaded">
        <span class="glyphicon glyphicon-info-sign"></span>
        {{ 'PLUGIN_TASK_LEGEND_FOOTER' | translate }}
      </p>
    </div>
  </section>
</div>
<!-- / CE - ui/cockpit/plugins/tasks/app/views/dashboard/task-dashboard.html -->
`;
