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

module.exports = `<!-- # cockpit client/scripts/repository/resource/directives/cam-cockpit-resource-meta.html -->
<div class="filepath">{{ resource._filepath }}</div>
<div class="table-row">
  <h2 class="name">{{ resource._filename }}</h2>
  <h3 class="version" ng-show="!isDmnResource(resource) && definitions.length > 0">{{ 'REPOSITORY_DEPLOYMENT_RESOURCE_DIRECTIVES_VERSION' | translate }} {{ definitions[0].version }}</h3>
</div>
<!-- / cockpit client/scripts/repository/resource/directives/cam-cockpit-resource-meta.html -->
`;
