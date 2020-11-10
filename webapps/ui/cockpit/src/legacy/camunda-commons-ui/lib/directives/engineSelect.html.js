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

module.exports = `<li class="dropdown engine-select"
    ng-show="engines.length > 1 && currentEngine"
    uib-dropdown>
<!-- # CE - camunda-commons-ui/lib/directives/engineSelect.html -->
  <a href
     class="dropdown-toggle"
     uib-dropdown-toggle>
    <span class="glyphicon glyphicon-info-sign glyphicon glyphicon-info-sign "
          uib-tooltip="{{ 'DIRECTIVE_ENGINE_SELECT_TOOLTIP' | translate }}"
          tooltip-placement="bottom"></span>
    {{ currentEngine.name }}
  </a>
  <ul uib-dropdown-menu class="dropdown-menu dropdown-menu-right">
    <li ng-repeat="(id, engine) in engines"
        ng-class="{ active: currentEngine.name === engine.name }">
      <a ng-href="{{ 'app://../' + engine.name + '/' | uri }}">
        {{ engine.name }}
      </a>
    </li>
  </ul>
<!-- / CE - camunda-commons-ui/lib/directives/engineSelect.html -->
</li>
`;
