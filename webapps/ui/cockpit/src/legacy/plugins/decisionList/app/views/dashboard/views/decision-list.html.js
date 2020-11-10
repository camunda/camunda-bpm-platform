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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/decisionList/app/views/dashboard/decision-list.html -->
<div class="deployed-decisions"
     cam-widget-loader
     loading-state="{{ loadingState }}"
     text-error="{{ loadingError }}">

  <div decisions-table
       decisions="decisions"
       decision-count="decisionCount"
       is-drd-available="isDrdDashboardAvailable"
       pagination="paginationController">
  </div>

  <view provider="drdDashboard"
        vars="drdDashboardVars">
  </view>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/decisionList/app/views/dashboard/decision-list.html -->
`;
