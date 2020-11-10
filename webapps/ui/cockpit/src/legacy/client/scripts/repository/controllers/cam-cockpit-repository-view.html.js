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

module.exports = `<!-- # cockpit client/scripts/repository/controllers/cam-cockpit-repository-view.html -->
<div cam-repository
     class="three-cols-layout">
  <div class="three-cols-layout-columns">

      <!-- # deployment-list column -->
    <section class="column column-left deployments"
             ng-controller="camDeploymentsCtrl">

      <header class="three-cols-layout-cell top">
        <div cam-deployments-sorting-choices
             deployments-data="deploymentsData"></div>

        <div cam-deploy="onDeployed()"></div>
      </header>

      <div class="three-cols-layout-cell content">

        <view ng-repeat="deploymentsPlugin in deploymentsPlugins"
              provider="deploymentsPlugin"
              class="deployment-plugins"
              vars="deploymentsVars"></view>

        <div cam-deployments
            deployments-data="deploymentsData"
            deployments="control.deployments"></div>
      </div>

    </section>
    <!-- / deployment-list column -->

    <!-- # resource-list column -->
    <section class="column column-center resources">

      <header class="three-cols-layout-cell top">
      </header>

      <div class="three-cols-layout-cell content"
           cam-resources
           repository-data="repositoryData">
      </div>

    </section>
    <!-- / resource-list column -->



    <!-- # resource-details column -->
    <section class="column column-right resource-details"
             ng-controller="camResourceDetailsCtrl">

      <header class="three-cols-layout-cell top">
        <div class="resource-actions"
             ng-if="resource"
             ng-repeat="resourceAction in resourceActions">
          <view provider="resourceAction" vars="resourceVars">
        </div>
      </header>

      <div class="three-cols-layout-cell content"
           cam-resource-wrapper
           resource-details-data="resourceDetailsData"
           control="control">
      </div>

    </section>
    <!-- # resource-details column -->

  </div>
</div>
<!-- / cockpit client/scripts/repository/controllers/cam-cockpit-repository-view.html -->
`;
