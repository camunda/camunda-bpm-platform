/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;

public class DecisionRequirementsDefinitionDto {

  protected String id;
  protected String key;
  protected String category;
  protected String name;
  protected int version;
  protected String resource;
  protected String deploymentId;
  protected String tenantId;

  public String getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getCategory() {
    return category;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getResource() {
    return resource;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static DecisionRequirementsDefinitionDto fromDecisionRequirementsDefinition(DecisionRequirementsDefinition definition) {
    DecisionRequirementsDefinitionDto dto = new DecisionRequirementsDefinitionDto();

    dto.id = definition.getId();
    dto.key = definition.getKey();
    dto.category = definition.getCategory();
    dto.name = definition.getName();
    dto.version = definition.getVersion();
    dto.resource = definition.getResourceName();
    dto.deploymentId = definition.getDeploymentId();
    dto.tenantId = definition.getTenantId();

    return dto;
  }

}
