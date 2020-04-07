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

module.exports = `<!-- # cockpit client/scripts/repository/resources/directives/cam-cockpit-resources.html -->
<div ng-if="!state.$loaded && !state.$error"
     class="loader">
  <span class="animate-spin glyphicon glyphicon-refresh"></span>
  {{ 'REPOSITORY_RESOURCES_LOADING' | translate }}
</div>

<div ng-if="state.$error"
     uib-alert class="alert alert-danger"
     role="alert">
  <span class="glyphicon glyphicon-exclamation-sign"></span>
  <strong class="status">{{ 'REPOSITORY_RESOURCES_FAILURE' | translate }}</strong>
  <span class="message">{{ 'REPOSITORY_RESOURCES_FAILURE_MSN' | translate }}</span>
</div>

<div ng-if="state.$loaded && !state.$error && !resources.length"
     class="well">
  <span class="glyphicon glyphicon-info-sign"></span>
  {{ 'REPOSITORY_RESOURCES_NO_SRC_AVAILABLE' | translate }}
</div>

<ol ng-if="state.$loaded && !state.$error && resources.length"
    class="resources list-unstyled"
    tabindex="0"
    ng-keydown="handleKeydown($event)">
  <li class="resource"
      ng-repeat="(delta, resource) in resources"
      ng-class="{active: currentResourceId === resource.id, clickable: true}"
      ng-click="focus($event, resource)">
    <div class="filepath">{{ truncateFilepath(resource._filepath) }}</div>
    <h4>
      <a href>{{ resource._filename }}</a>
    </h4>
  </li>
</ol>
<!-- / cockpit client/scripts/repository/resources/directives/cam-cockpit-resources.html -->
`;
