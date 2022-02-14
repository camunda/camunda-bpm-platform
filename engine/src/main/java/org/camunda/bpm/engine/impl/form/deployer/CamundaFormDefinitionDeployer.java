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
package org.camunda.bpm.engine.impl.form.deployer;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.AbstractDefinitionDeployer;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.CamundaFormDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CamundaFormDefinitionDeployer extends AbstractDefinitionDeployer<CamundaFormDefinitionEntity> {

  protected static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;
  public static final String[] FORM_RESOURCE_SUFFIXES = new String[] { "form" };

  @Override
  protected String[] getResourcesSuffixes() {
    return FORM_RESOURCE_SUFFIXES;
  }

  @Override
  protected List<CamundaFormDefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource,
      Properties properties) {
    String formContent = new String(resource.getBytes(), StandardCharsets.UTF_8);

    try {
      JsonObject formJsonObject = new Gson().fromJson(formContent, JsonObject.class);
      String camundaFormDefinitionKey = JsonUtil.getString(formJsonObject, "id");
      CamundaFormDefinitionEntity definition = new CamundaFormDefinitionEntity(camundaFormDefinitionKey, deployment.getId(), resource.getName(), deployment.getTenantId());
      return Collections.singletonList(definition);
    } catch (Exception e) {
      // form could not be parsed, throw exception if strict parsing is not disabled
      if (!getCommandContext().getProcessEngineConfiguration().isDisableStrictCamundaFormParsing()) {
        throw LOG.exceptionDuringFormParsing(e.getMessage(), resource.getName());
      }
      return Collections.emptyList();
    }
  }

  @Override
  protected CamundaFormDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return getCommandContext().getCamundaFormDefinitionManager().findDefinitionByDeploymentAndKey(deploymentId,
        definitionKey);
  }

  @Override
  protected CamundaFormDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return getCommandContext().getCamundaFormDefinitionManager().findLatestDefinitionByKeyAndTenantId(definitionKey,
        tenantId);
  }

  @Override
  protected void persistDefinition(CamundaFormDefinitionEntity definition) {
    getCommandContext().getCamundaFormDefinitionManager().insert(definition);
  }

  @Override
  protected void addDefinitionToDeploymentCache(DeploymentCache deploymentCache,
      CamundaFormDefinitionEntity definition) {
    deploymentCache.addCamundaFormDefinition(definition);
  }

}
