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
package org.camunda.bpm.engine.impl.form.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.CamundaFormDefinitionEntity;

public class CamundaFormDefinitionManager extends AbstractManager
    implements AbstractResourceDefinitionManager<CamundaFormDefinitionEntity> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  @Override
  public CamundaFormDefinitionEntity findLatestDefinitionByKey(String key) {
    @SuppressWarnings("unchecked")
    List<CamundaFormDefinitionEntity> camundaFormDefinitions = getDbEntityManager()
        .selectList("selectLatestCamundaFormDefinitionByKey", configureParameterizedQuery(key));

    if (camundaFormDefinitions.isEmpty()) {
      return null;

    } else if (camundaFormDefinitions.size() == 1) {
      return camundaFormDefinitions.iterator().next();

    } else {
      throw LOG.multipleTenantsForCamundaFormDefinitionKeyException(key);
    }
  }

  @Override
  public CamundaFormDefinitionEntity findLatestDefinitionById(String id) {
    return getDbEntityManager().selectById(CamundaFormDefinitionEntity.class, id);
  }

  @Override
  public CamundaFormDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("camundaFormDefinitionKey", definitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (CamundaFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectLatestCamundaFormDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (CamundaFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectLatestCamundaDefinitionByKeyAndTenantId", parameters);
    }
  }

  @Override
  public CamundaFormDefinitionEntity findDefinitionByKeyVersionAndTenantId(String definitionKey,
      Integer definitionVersion, String tenantId) {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("camundaFormDefinitionVersion", definitionVersion);
    parameters.put("camundaFormDefinitionKey", definitionKey);
    parameters.put("tenantId", tenantId);
    if (tenantId == null) {
      return (CamundaFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectCamundaFormDefinitionByKeyVersionWithoutTenantId", parameters);
    } else {
      return (CamundaFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectCamundaFormDefinitionByKeyVersionAndTenantId", parameters);
    }
  }

  @Override
  public CamundaFormDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("camundaFormDefinitionKey", definitionKey);
    return (CamundaFormDefinitionEntity) getDbEntityManager().selectOne("selectCamundaFormDefinitionByDeploymentAndKey",
        parameters);
  }

  @SuppressWarnings("unchecked")
  public List<CamundaFormDefinitionEntity> findDefinitionsByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectCamundaFormDefinitionByDeploymentId", deploymentId);
  }

  @Override
  public CamundaFormDefinitionEntity getCachedResourceDefinitionEntity(String definitionId) {
    return getDbEntityManager().getCachedEntity(CamundaFormDefinitionEntity.class, definitionId);
  }

  @Override
  public CamundaFormDefinitionEntity findDefinitionByKeyVersionTagAndTenantId(String definitionKey,
      String definitionVersionTag, String tenantId) {
    throw new UnsupportedOperationException(
        "Currently finding Camunda Form definition by version tag and tenant is not implemented.");
  }

  public void deleteCamundaFormDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(CamundaFormDefinitionEntity.class, "deleteCamundaFormDefinitionsByDeploymentId",
        deploymentId);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

}
