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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.*;


/**
 * @author Tom Baeyens
 */
public class DeploymentEntity implements Serializable, DeploymentWithDefinitions, DbEntity {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected Map<String, ResourceEntity> resources;
  protected Date deploymentTime;
  protected boolean validatingSchema = true;
  protected boolean isNew;
  protected String source;
  protected String tenantId;

  /**
   * Will only be used during actual deployment to pass deployed artifacts (eg process definitions).
   * Will be null otherwise.
   */
  protected Map<Class<?>, List> deployedArtifacts;

  public ResourceEntity getResource(String resourceName) {
    return getResources().get(resourceName);
  }

  public void addResource(ResourceEntity resource) {
    if (resources==null) {
      resources = new HashMap<>();
    }
    resources.put(resource.getName(), resource);
  }

  public void clearResources() {
    if(resources!=null){
      resources.clear();
    }
  }

  // lazy loading /////////////////////////////////////////////////////////////
  public Map<String, ResourceEntity> getResources() {
    if (resources==null && id!=null) {
      List<ResourceEntity> resourcesList = Context
        .getCommandContext()
        .getResourceManager()
        .findResourcesByDeploymentId(id);
      resources = new HashMap<>();
      for (ResourceEntity resource: resourcesList) {
        resources.put(resource.getName(), resource);
      }
    }
    return resources;
  }

  public Object getPersistentState() {
    // properties of this entity are immutable
    // so always the same value is returned
    // so never will an update be issued for a DeploymentEntity
    return DeploymentEntity.class;
  }

  // Deployed artifacts manipulation //////////////////////////////////////////

  public void addDeployedArtifact(ResourceDefinitionEntity deployedArtifact) {
    if (deployedArtifacts == null) {
      deployedArtifacts = new HashMap<Class<?>, List>();
    }

    Class<?> clazz = deployedArtifact.getClass();
    List artifacts = deployedArtifacts.get(clazz);
    if (artifacts == null) {
      artifacts = new ArrayList();
      deployedArtifacts.put(clazz, artifacts);
    }

    artifacts.add(deployedArtifact);
  }

  public Map<Class<?>, List> getDeployedArtifacts() {
    return deployedArtifacts;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getDeployedArtifacts(Class<T> clazz) {
    if(deployedArtifacts == null) {
      return null;
    } else {
      return (List<T>) deployedArtifacts.get(clazz);
    }
  }

  public void removeArtifact(ResourceDefinitionEntity notDeployedArtifact) {
    if (deployedArtifacts != null) {
      List artifacts = deployedArtifacts.get(notDeployedArtifact.getClass());
      if (artifacts != null) {
        artifacts.remove(notDeployedArtifact);
        if (artifacts.isEmpty()) {
          deployedArtifacts.remove(notDeployedArtifact.getClass());
        }
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setResources(Map<String, ResourceEntity> resources) {
    this.resources = resources;
  }

  public Date getDeploymentTime() {
    return deploymentTime;
  }

  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }

  public boolean isValidatingSchema() {
    return validatingSchema;
  }

  public void setValidatingSchema(boolean validatingSchema) {
    this.validatingSchema = validatingSchema;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public List<ProcessDefinition> getDeployedProcessDefinitions() {
    return deployedArtifacts == null ? null : deployedArtifacts.get(ProcessDefinitionEntity.class);
  }

  @Override
  public List<CaseDefinition> getDeployedCaseDefinitions() {
    return deployedArtifacts == null ? null : deployedArtifacts.get(CaseDefinitionEntity.class);
  }

  @Override
  public List<DecisionDefinition> getDeployedDecisionDefinitions() {
    return deployedArtifacts == null ? null : deployedArtifacts.get(DecisionDefinitionEntity.class);
  }

  @Override
  public List<DecisionRequirementsDefinition> getDeployedDecisionRequirementsDefinitions() {
    return deployedArtifacts == null ? null : deployedArtifacts.get(DecisionRequirementsDefinitionEntity.class);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", name=" + name
           + ", resources=" + resources
           + ", deploymentTime=" + deploymentTime
           + ", validatingSchema=" + validatingSchema
           + ", isNew=" + isNew
           + ", source=" + source
           + ", tenantId=" + tenantId
           + "]";
  }

}
