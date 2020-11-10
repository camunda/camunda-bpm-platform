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

module.exports = `<!-- # EE - ui/common/scripts/module/external-tasks-common/components/external-tasks-tab.html -->
<div cam-widget-loader
     loading-state="{{ TasksTab.loadingState }}"
     text-empty="{{ 'PLUGIN_EXTERNAL_TASK_NO_EXTERNAL_TASK_FOR_THIS_PROCESS' | translate }}">

  <!-- transclustion target -->
  <div ng-transclude></div>
  <!-- / transclustion target -->

  <div cam-pagination="TasksTab.onPaginationChange(pages)" total="TasksTab.total" >
  </div>
</div>
<!-- / EE - ui/common/scripts/module/external-tasks-common/components/external-tasks-tab.html -->
`;
