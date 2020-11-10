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

module.exports = `<!-- # cockpit client/scripts/repository/deployments/directives/cam-cockpit-deployments.html -->
<div cam-searchable-area
     config="searchConfig"
     on-search-change="onSearchChange(query, pages)"
     loading-state="loadingState"
     search-id="deployments"
     loading-error="{{ 'REPOSITORY_DEPLOYMENTS_LOADING_LEGEND' | translate }}"
     text-empty="{{ 'REPOSITORY_DEPLOYMENTS_NO_DEPLOYMENTS_AVAILABLE' | translate}}"
     storage-group="'DEP'">
  <div class="wrapper">
    <ul class="deployments">
      <li ng-repeat="(delta, deployment) in deployments"
          ng-class="{active: isFocused(deployment), deployment: true}"
          ng-style="{'z-index': ((deployments.length + 10) - delta) }"
          ng-click="focus(deployment)">

        <div cam-deployment
             deployment="deployment"
             deployments-list-data="deploymentsListData"
             control="control">
        </div>
      </li>
    </ul>
  </div>
</div>
<!-- / cockpit client/scripts/repository/deployments/directives/cam-cockpit-deployments.html -->
`;
