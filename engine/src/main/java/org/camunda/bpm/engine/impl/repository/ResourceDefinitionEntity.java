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
package org.camunda.bpm.engine.impl.repository;

import org.camunda.bpm.engine.repository.ResourceDefinition;

/**
 * Entity of a deployed resource definition
 */
public interface ResourceDefinitionEntity<T extends ResourceDefinition> extends ResourceDefinition {

  void setId(String id);

  void setCategory(String category);

  void setName(String name);

  void setKey(String key);

  void setVersion(int version);

  void setResourceName(String resourceName);

  void setDeploymentId(String deploymentId);

  void setDiagramResourceName(String diagramResourceName);

  void setTenantId(String tenantId);

  ResourceDefinitionEntity getPreviousDefinition();

  void updateModifiableFieldsFromEntity(T updatingDefinition);

  void setHistoryTimeToLive(Integer historyTimeToLive);

}
