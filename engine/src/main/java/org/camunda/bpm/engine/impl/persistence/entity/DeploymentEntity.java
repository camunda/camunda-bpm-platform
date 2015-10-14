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

package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.repository.Deployment;


/**
 * @author Tom Baeyens
 */
public class DeploymentEntity implements Serializable, Deployment, DbEntity {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected Map<String, ResourceEntity> resources;
  protected Date deploymentTime;
  protected boolean validatingSchema = true;
  protected boolean isNew;
  protected String source;

  /**
   * Will only be used during actual deployment to pass deployed artifacts (eg process definitions).
   * Will be null otherwise.
   */
  protected Map<Class<?>, List<Object>> deployedArtifacts;
  protected Map<Class<?>, List<Object>> notdeployedARtifacts;

  public ResourceEntity getResource(String resourceName) {
    return getResources().get(resourceName);
  }

  public void addResource(ResourceEntity resource) {
    if (resources==null) {
      resources = new HashMap<String, ResourceEntity>();
    }
    resources.put(resource.getName(), resource);
  }

  // lazy loading /////////////////////////////////////////////////////////////
  public Map<String, ResourceEntity> getResources() {
    if (resources==null && id!=null) {
      List<ResourceEntity> resourcesList = Context
        .getCommandContext()
        .getResourceManager()
        .findResourcesByDeploymentId(id);
      resources = new HashMap<String, ResourceEntity>();
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

  public void addDeployedArtifact(Object deployedArtifact) {
    if (deployedArtifacts == null) {
      deployedArtifacts = new HashMap<Class<?>, List<Object>>();
    }

    Class<?> clazz = deployedArtifact.getClass();
    List<Object> artifacts = deployedArtifacts.get(clazz);
    if (artifacts == null) {
      artifacts = new ArrayList<Object>();
      deployedArtifacts.put(clazz, artifacts);
    }

    artifacts.add(deployedArtifact);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getDeployedArtifacts(Class<T> clazz) {
    if(deployedArtifacts == null) {
      return null;
    } else {
      return (List<T>) deployedArtifacts.get(clazz);
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
           + "]";
  }
}
