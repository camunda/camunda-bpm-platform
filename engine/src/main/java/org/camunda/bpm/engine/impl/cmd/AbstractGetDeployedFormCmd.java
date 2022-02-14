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

import java.io.InputStream;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.DeploymentResourceNotFoundException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.form.CamundaFormRef;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.CamundaFormDefinition;

/**
 *
 * @author Anna Pazola
 *
 */
public abstract class AbstractGetDeployedFormCmd implements Command<InputStream> {

  protected static String EMBEDDED_KEY = "embedded:";
  protected static String CAMUNDA_FORMS_KEY = "camunda-forms:";
  protected static int EMBEDDED_KEY_LENGTH = EMBEDDED_KEY.length();
  protected static int CAMUNDA_FORMS_KEY_LENGTH = CAMUNDA_FORMS_KEY.length();

  protected static String DEPLOYMENT_KEY = "deployment:";
  protected static int DEPLOYMENT_KEY_LENGTH = DEPLOYMENT_KEY.length();

  protected CommandContext commandContext;

  public InputStream execute(final CommandContext commandContext) {
    this.commandContext = commandContext;
    checkAuthorization();

    final FormData formData = getFormData();
    String formKey = formData.getFormKey();
    CamundaFormRef camundaFormRef = formData.getCamundaFormRef();

    if (formKey != null) {
      return getResourceForFormKey(formData, formKey);
    } else if(camundaFormRef != null && camundaFormRef.getKey() != null) {
      return getResourceForCamundaFormRef(camundaFormRef, formData.getDeploymentId());
    } else {
      throw new BadUserRequestException("One of the attributes 'formKey' and 'camunda:formRef' must be supplied but none were set.");
    }
  }

  protected InputStream getResourceForFormKey(FormData formData, String formKey) {
    String resourceName = formKey;

    if (resourceName.startsWith(EMBEDDED_KEY)) {
      resourceName = resourceName.substring(EMBEDDED_KEY_LENGTH, resourceName.length());
    } else if (resourceName.startsWith(CAMUNDA_FORMS_KEY)) {
      resourceName = resourceName.substring(CAMUNDA_FORMS_KEY_LENGTH, resourceName.length());
    }

    if (!resourceName.startsWith(DEPLOYMENT_KEY)) {
      throw new BadUserRequestException("The form key '" + formKey + "' does not reference a deployed form.");
    }

    resourceName = resourceName.substring(DEPLOYMENT_KEY_LENGTH, resourceName.length());

    return getDeploymentResource(formData.getDeploymentId(), resourceName);
  }

  protected InputStream getResourceForCamundaFormRef(CamundaFormRef camundaFormRef,
      String deploymentId) {
    CamundaFormDefinition definition = commandContext.runWithoutAuthorization(
        new GetCamundaFormDefinitionCmd(camundaFormRef, deploymentId));

    if (definition == null) {
      throw new NotFoundException("No Camunda Form Definition was found for Camunda Form Ref: " + camundaFormRef);
    }

    return getDeploymentResource(definition.getDeploymentId(), definition.getResourceName());
  }

  protected InputStream getDeploymentResource(String deploymentId, String resourceName) {
    GetDeploymentResourceCmd getDeploymentResourceCmd = new GetDeploymentResourceCmd(deploymentId, resourceName);
    try {
      return commandContext.runWithoutAuthorization(getDeploymentResourceCmd);
    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException("The form with the resource name '" + resourceName + "' cannot be found in deployment with id " + deploymentId, e);
    }
  }

  protected abstract FormData getFormData();

  protected abstract void checkAuthorization();

}
