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

module.exports = `<!-- # CE - src/main/webapp/app/cockpit/directives/quick-filter.html -->
<form name="quickFilters"
      class="quick-filters">
  <div class="quick-filter name-filter"
       ng-if="showNameFilter">
    <label class="input-group">
      <span class="input-group-addon"
            uib-tooltip="{{ 'QUICK_FILTER_TOOLTIP_CLICK_HERE' | translate }}">
        <span class="glyphicon glyphicon-filter"
              ng-click="clearName()"></span>
      </span>
      <input ng-keyup="search()"
             ng-model="name"
             name="name"
             type="text"
             class="form-control" />
    </label>
    <button class="btn btn-sm btn-link btn-control-link"
            type="button"
            uib-tooltip="{{ 'QUICK_FILTER_TOOLTIP_FILTERS_ACTIVITY' | translate }}">
      <span class="glyphicon glyphicon-question-sign"></span>
    </button>
  </div>

  <div class="quick-filter state-filter"
       ng-if="showStateFilter">
    <span class="name">{{ 'QUICK_FILTER_STATE' | translate }}</span>

    <ul class="list-inline">
      <li>
        <label class="btn btn-default btn-xs running"
               uib-tooltip="{{ 'QUICK_FILTER_TOOLTIP_RUNNING_ACTIVITY' | translate }}"
               tooltip-placement="bottom">
          <span class="glyphicon glyphicon-adjust"></span>
          <input ng-change="search()"
                 ng-model="running"
                 name="running"
                 type="checkbox" />
        </label>
      </li>

      <li>
        <label class="btn btn-default btn-xs completed"
               uib-tooltip="{{ 'QUICK_FILTER_TOOLTIP_COMPLETED_ACTIVITY' | translate }}"
               tooltip-placement="bottom">
          <span class="glyphicon glyphicon-ok-circle"></span>
          <input ng-change="search()"
                 ng-model="completed"
                 name="completed"
                 type="checkbox" />
        </label>
      </li>

      <li>
        <label class="btn btn-default btn-xs canceled"
               uib-tooltip="{{ 'QUICK_FILTER_TOOLTIP_CANCELED_ACTIVITY' | translate }}"
               tooltip-placement="bottom">
          <span class="glyphicon glyphicon-ban-circle"></span>
          <input ng-change="search()"
                 ng-model="canceled"
                 name="canceled"
                 type="checkbox" />
        </label>
      </li>
    </ul>
  </div>
</form>
<!-- / CE - src/main/webapp/app/cockpit/directives/quick-filter.html -->
`;
