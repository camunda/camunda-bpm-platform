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

module.exports = function(pluginPoint, scope) {
  let result = {};

  switch (pluginPoint) {
    case 'cockpit.processDefinition.runtime.tab':
    case 'cockpit.processDefinition.runtime.action':
    case 'cockpit.processDefinition.history.action':
    case 'cockpit.processDefinition.history.tab':
      result.processDefinitionId = scope.processDefinition.id;
      break;

    case 'cockpit.processInstance.runtime.tab':
    case 'cockpit.processInstance.runtime.action':
    case 'cockpit.processInstance.history.tab':
    case 'cockpit.processInstance.history.action':
      result.processInstanceId = scope.processInstance.id;
      break;

    case 'cockpit.processDefinition.diagram.plugin':
    case 'cockpit.processDefinition.history.diagram.plugin':
      result.processDefinitionId = scope.key;
      break;

    case 'cockpit.processInstance.diagram.plugin':
    case 'cockpit.processInstance.history.diagram.plugin':
      result.processInstanceId = window.location.hash.split('/')[2];
      break;

    case 'cockpit.jobDefinition.action':
      result.jobDefinitionId = scope.jobDefinition.id;
      break;

    case 'cockpit.decisionDefinition.tab':
    case 'cockpit.decisionDefinition.action':
      result.decisionDefinitionId = scope.decisionDefinition.id;
      break;

    case 'cockpit.decisionInstance.tab':
    case 'cockpit.decisionInstance.action':
      result.decisionInstanceId = scope.decisionInstance.id;
      break;

    case 'cockpit.caseDefinition.tab':
    case 'cockpit.caseDefinition.action':
      result.caseDefinitionId = scope.definition.id;
      break;

    case 'cockpit.caseInstance.tab':
    case 'cockpit.caseInstance.action':
      result.caseInstanceId = scope.instance.id;
      break;

    case 'cockpit.repository.resource.action':
      result.deploymentId = scope.deployment.id;
      result.resourceId = scope.resource.id;
      break;

    case 'cockpit.incident.action':
      result.incidentId = scope.incident.id;
      break;

    case 'cockpit.drd.definition.tab':
      result.drdDefinitionId = scope.tabsApi.getDefinition().id;
      break;

    case 'cockpit.drd.instance.tab':
      result.rootDecisionInstanceId = scope.tabsApi.processParams(
        {}
      ).rootDecisionInstanceId;
      break;

    case 'cockpit.processDefinition.diagram.action':
    case 'cockpit.processDefinition.history.diagram.action':
      result.viewer = scope.viewer;
      result.processDefinitionId = window.location.hash.split('/')[2];
      break;

    case 'cockpit.processes.action':
      result.processDefinitionId = scope.pd.id;
      break;

    case 'cockpit.repository.deployment.action':
      result.deploymentId = scope.deployment.id;
      break;

    default:
      result = {};
      break;
  }

  return result;
};
