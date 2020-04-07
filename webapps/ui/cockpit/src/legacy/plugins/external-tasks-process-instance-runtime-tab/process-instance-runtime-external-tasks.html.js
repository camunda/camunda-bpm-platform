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

module.exports = `<!-- # CE - ui/cockpit/plugins/external-tasks-process-instance-runtime-tab/process-instance-runtime-external-tasks.html -->
<div external-tasks-tab="RuntimeTab.onLoad(pages, activityIds)"
     process-data="RuntimeTab.processData">
  <table class="history-job-log cam-table">

    <thead sortable-table-head
           head-columns="RuntimeTab.headColumns"
           on-sort-change="RuntimeTab.onSortChange(RuntimeTab.sorting)"
           default-sort="RuntimeTab.sorting">
    </thead>
    <tbody>
      <tr ng-repeat="task in RuntimeTab.tasks track by task.id">
        <td class="state"
            cam-widget-clipboard="task.id">{{task.id}}</td>
        <td class="activity">
          <span external-task-activity-link="task.activityId"
                bpmn-elements="RuntimeTab.bpmnElements">
          </span>
        </td>
        <td class="retries">{{task.retries}}</td>
        <td class="worker-id">{{task.workerId}}</td>
        <td class="expiration">{{task.lockExpirationTime}}</td>
        <td class="topic">{{task.topicName}}</td>
        <td class="priority">{{task.priority}}</td>
      </tr>
    </tbody>
  </table>
</div>
<!-- / CE - ui/cockpit/plugins/external-tasks-process-instance-runtime-tab/process-instance-runtime-external-tasks.html -->
`;
