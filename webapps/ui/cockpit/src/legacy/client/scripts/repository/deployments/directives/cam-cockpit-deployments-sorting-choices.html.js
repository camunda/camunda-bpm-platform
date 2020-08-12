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

module.exports = `<!-- # cockpit client/scripts/repository/deployments/directives/cam-cockpit-deployments-sorting-choices.html -->
<div uib-dropdown class="dropdown">

  <a href
     uib-dropdown-toggle
     tabindex="-1"
     role="button"
     class="dropdown-toggle">
    <span class="hidden-xs hidden-sm hidden-md"></span>
    <span class="sort-by">{{ byLabel(sorting.sortBy) }}</span>
  </a>

  <a href
     tabindex="-1"
     uib-tooltip="{{ (sorting.sortOrder === 'desc' ? 'REPOSITORY_DEPLOYMENTS_TOOLTIP_SORT_DESC' : 'REPOSITORY_DEPLOYMENTS_TOOLTIP_SORT_ASC' | translate) }}"
     class="glyphicon sort-direction"
     ng-class="sorting.sortOrder === 'asc' ? 'glyphicon-menu-up' : 'glyphicon-menu-down'"
     ng-click="changeOrder()"></a>

  <ul uib-dropdown-menu class="dropdown-menu"
      role="menu">

    <li ng-show="sorting.sortBy !== id"
        ng-repeat="(id, name) in uniqueProps">
      <a href
         tabindex="-1"
         class="sort-by-choice"
         ng-click="changeBy(id)">{{ name }}</a>
    </li>

  </ul>

</div>
<!-- / cockpit client/scripts/repository/deployments/directives/cam-cockpit-deployments-sorting-choices.html -->
`;
