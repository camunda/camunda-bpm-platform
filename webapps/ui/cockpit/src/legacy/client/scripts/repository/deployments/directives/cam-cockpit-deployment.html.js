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

module.exports = `<div class="actions">
  <view ng-repeat="deploymentPlugin in deploymentPlugins"
        provider="deploymentPlugin"
        vars="deploymentVars" />
</div>

<h4 class="name">{{ deployment.name || deployment.id }}</h4>

<dl>
  <dt>{{ 'REPOSITORY_DEPLOYMENTS_TIME' | translate }}</dt>
  <dd class="time">{{ deployment.deploymentTime | camDate:'normal' }}</dd>

  <dt>{{ 'REPOSITORY_DEPLOYMENTS_SOURCE' | translate }}</dt>
  <dd class="source"
      ng-if="deployment.source">{{ deployment.source }}</dd>
  <dd class="source undefined"
      ng-if="!deployment.source"><span class="null-value">{{ 'REPOSITORY_DEPLOYMENTS_NULL' | translate }}</span>

  <dt>{{ 'REPOSITORY_DEPLOYMENTS_TENANT_ID' | translate }}</dt>
  <dd class="tenant-id"
      ng-if="deployment.tenantId">{{ deployment.tenantId }}</dd>
  <dd class="tenant-id"
      ng-if="!deployment.tenantId"><span class="null-value">{{ 'REPOSITORY_DEPLOYMENTS_NULL' | translate }}</span></dd>

</dl>
`;
