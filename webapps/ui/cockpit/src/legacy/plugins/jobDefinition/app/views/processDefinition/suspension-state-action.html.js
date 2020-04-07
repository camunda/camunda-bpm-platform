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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/jobDefinition/app/views/processDefinition/suspension-state-action.html -->
<a class="btn btn-default action-button"
   ng-click="openSuspensionStateDialog(jobDefinition)"
   ng-show="jobDefinition.suspended"
   uib-tooltip="{{ 'PLUGIN_JOBDEFINITION_STATE_ACTION_TOOLTIP_1' | translate }}"
   tooltip-placement="left">
  <span class="glyphicon glyphicon-play"></span>
</a>
<a class="btn btn-default action-button"
   ng-click="openSuspensionStateDialog(jobDefinition)"
   ng-hide="jobDefinition.suspended"
   uib-tooltip="{{ 'PLUGIN_JOBDEFINITION_STATE_ACTION_TOOLTIP_2' | translate }}"
   tooltip-placement="left">
  <span class="glyphicon glyphicon-pause"></span>
</a>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/jobDefinition/app/views/processDefinition/suspension-state-action.html -->
`;
