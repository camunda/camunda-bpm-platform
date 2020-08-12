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

module.exports = `<section>
  <div class="inner">
    <header ng-if="decisionCount">
      <h1 class="section-title"
          ng-if="decisionCount > 1"
          translate="PLUGIN_DECISION_TABLE_TITLE_DEFINITIONS"
          translate-values='{count: decisionCount}'></h1>

      <h1 class="section-title"
          ng-if="decisionCount <= 1"
          translate="PLUGIN_DECISION_TABLE_TITLE_DEFINITION"
          translate-values='{count: decisionCount}'></h1>
    </header>

    <header class='header-empty' ng-if="!decisionCount">
      <h1 class="section-title">{{ 'PLUGIN_DECISION_TABLE_NO_DECISION_TABLES_DEPLOYED' | translate }}</h1>
    </header>

    <table class="decision-definitions-list cam-table"
           ng-if="decisionCount">

      <thead sortable-table-head
             head-columns="headColumns"
             on-sort-change="onSortChange(sortObj)"
             default-sort="sortObj">
      </thead>

      <tbody>
      <tr ng-repeat="decision in decisions">
        <td class="name">
          <a href="#/decision-definition/{{ decision.id }}">
            {{ decision.name || decision.key }}
          </a>
        </td>
        <td class="tenant-id">
          {{ decision.tenantId }}
        </td>
        <td class="drd" ng-if="isDrdAvailable">
          <a href="#/decision-requirement/{{ decision.drd.id }}">
            {{ decision.drd.name || decision.drd.key }}
          </a>
        </td>
      </tr>
      </tbody>
    </table>

    <ul uib-pagination ng-if="pagination.decisionPages.total > pagination.decisionPages.size"
      class="pagination-sm"

      page="pagination.decisionPages.current"
      ng-model="pagination.decisionPages.current"
      ng-change="pagination.changeDecisionPage(pagination.decisionPages)"

      total-items="pagination.decisionPages.total"
      items-per-page="pagination.decisionPages.size"

      max-size="7"
      boundary-links="true"></ul>

  </div>
</section>
`;
