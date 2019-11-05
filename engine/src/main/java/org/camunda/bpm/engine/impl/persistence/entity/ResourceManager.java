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

import org.camunda.bpm.engine.impl.cmd.LicenseCmd;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Tom Baeyens
 */
public class ResourceManager extends AbstractManager {

  public void insertResource(ResourceEntity resource) {
    getDbEntityManager().insert(resource);
  }

  public void deleteResourcesByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(ResourceEntity.class, "deleteResourcesByDeploymentId", deploymentId);
  }

  public ResourceEntity findResourceByDeploymentIdAndResourceName(String deploymentId, String resourceName) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceName", resourceName);
    return (ResourceEntity) getDbEntityManager().selectOne("selectResourceByDeploymentIdAndResourceName", params);
  }

  @SuppressWarnings("unchecked")
  public List<ResourceEntity> findResourceByDeploymentIdAndResourceNames(String deploymentId, String... resourceNames) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceNames", resourceNames);
    return getDbEntityManager().selectList("selectResourceByDeploymentIdAndResourceNames", params);
  }

  public ResourceEntity findResourceByDeploymentIdAndResourceId(String deploymentId, String resourceId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceId", resourceId);
    return (ResourceEntity) getDbEntityManager().selectOne("selectResourceByDeploymentIdAndResourceId", params);
  }

  @SuppressWarnings("unchecked")
  public List<ResourceEntity> findResourceByDeploymentIdAndResourceIds(String deploymentId, String... resourceIds) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceIds", resourceIds);
    return getDbEntityManager().selectList("selectResourceByDeploymentIdAndResourceIds", params);
  }

  @SuppressWarnings("unchecked")
  public List<ResourceEntity> findResourcesByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectResourcesByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public Map<String, ResourceEntity> findLatestResourcesByDeploymentName(String deploymentName, Set<String> resourcesToFind, String source, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentName", deploymentName);
    params.put("resourcesToFind", resourcesToFind);
    params.put("source", source);
    params.put("tenantId", tenantId);

    List<ResourceEntity> resources = getDbEntityManager().selectList("selectLatestResourcesByDeploymentName", params);

    Map<String, ResourceEntity> existingResourcesByName = new HashMap<String, ResourceEntity>();
    for (ResourceEntity existingResource : resources) {
      existingResourcesByName.put(existingResource.getName(), existingResource);
    }

    return existingResourcesByName;
  }

  public ResourceEntity findLicenseKeyResource() {
    PropertyEntity licenseProperty = (PropertyEntity) getDbEntityManager().selectOne("selectProperty", LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);
    return licenseProperty == null ? null : (ResourceEntity) getDbEntityManager().selectOne("selectResourceById", licenseProperty.value);
  }
}
