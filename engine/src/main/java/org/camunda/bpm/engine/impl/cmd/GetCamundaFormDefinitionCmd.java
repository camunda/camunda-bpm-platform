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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.form.CamundaFormRef;
import org.camunda.bpm.engine.impl.form.entity.CamundaFormDefinitionManager;
import org.camunda.bpm.engine.impl.form.handler.DefaultFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CamundaFormDefinitionEntity;
import org.camunda.bpm.engine.repository.CamundaFormDefinition;

public class GetCamundaFormDefinitionCmd implements Command<CamundaFormDefinition> {

  protected CamundaFormRef camundaFormRef;
  protected String deploymentId;

  public GetCamundaFormDefinitionCmd(CamundaFormRef camundaFormRef, String deploymentId) {
    this.camundaFormRef = camundaFormRef;
    this.deploymentId = deploymentId;
  }

  @Override
  public CamundaFormDefinition execute(CommandContext commandContext) {
    String binding = camundaFormRef.getBinding();
    String key = camundaFormRef.getKey();
    CamundaFormDefinitionEntity definition = null;
    CamundaFormDefinitionManager manager = commandContext.getCamundaFormDefinitionManager();
    if (binding.equals(DefaultFormHandler.FORM_REF_BINDING_DEPLOYMENT)) {
      definition = manager.findDefinitionByDeploymentAndKey(deploymentId, key);
    } else if (binding.equals(DefaultFormHandler.FORM_REF_BINDING_LATEST)) {
      definition = manager.findLatestDefinitionByKey(key);
    } else if (binding.equals(DefaultFormHandler.FORM_REF_BINDING_VERSION)) {
      definition = manager.findDefinitionByKeyVersionAndTenantId(key, camundaFormRef.getVersion(), null);
    } else {
      throw new BadUserRequestException("Unsupported binding type for camundaFormRef. Expected to be one of "
          + DefaultFormHandler.ALLOWED_FORM_REF_BINDINGS + " but was:" + binding);
    }

    return definition;
  }

}
