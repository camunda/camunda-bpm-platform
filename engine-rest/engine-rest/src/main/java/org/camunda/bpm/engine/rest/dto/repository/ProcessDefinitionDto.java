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
package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.repository.ProcessDefinition;

public class ProcessDefinitionDto {

  protected String id;
  protected String key;
  protected String category;
  protected String description;
  protected String name;
  protected int version;
  protected String resource;
  protected String deploymentId;
  protected String diagram;
  protected boolean suspended;
  protected String tenantId;
  protected String versionTag;
  protected Integer historyTimeToLive;
  protected boolean isStartableInTasklist;

  public String getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
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

  public String getDiagram() {
    return diagram;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public boolean isStartableInTasklist() {
    return isStartableInTasklist;
  }

  public static ProcessDefinitionDto fromProcessDefinition(ProcessDefinition definition) {
    ProcessDefinitionDto dto = new ProcessDefinitionDto();
    dto.id = definition.getId();
    dto.key = definition.getKey();
    dto.category = definition.getCategory();
    dto.description = definition.getDescription();
    dto.name = definition.getName();
    dto.version = definition.getVersion();
    dto.resource = definition.getResourceName();
    dto.deploymentId = definition.getDeploymentId();
    dto.diagram = definition.getDiagramResourceName();
    dto.suspended = definition.isSuspended();
    dto.tenantId = definition.getTenantId();
    dto.versionTag = definition.getVersionTag();
    dto.historyTimeToLive = definition.getHistoryTimeToLive();
    dto.isStartableInTasklist = definition.isStartableInTasklist();
    return dto;
  }

}
