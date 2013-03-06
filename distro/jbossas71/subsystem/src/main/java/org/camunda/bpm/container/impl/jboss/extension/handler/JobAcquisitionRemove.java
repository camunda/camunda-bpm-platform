/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.extension.handler;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import java.util.List;
import java.util.Locale;

import org.camunda.bpm.container.impl.jboss.service.ContainerJobExecutorService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition.JobAcquisition;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;


/**
 * Provides the description and the implementation of the process-engine#remove operation.
 * 
 * @author Daniel Meyer
 */
public class JobAcquisitionRemove extends AbstractRemoveStepHandler implements DescriptionProvider {

  public static final JobAcquisitionRemove INSTANCE = new JobAcquisitionRemove();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Removes a job acquisition");
    node.get(OPERATION_NAME).set(REMOVE);
    return node;
  }

  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
    ContainerJobExecutorService service = (ContainerJobExecutorService) context.getServiceRegistry(false).getService(ContainerJobExecutorService.getServiceName()).getService();
    String jobAcquisitionName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    JobAcquisition jobAcquisition = service.getJobAcquisitionByName(jobAcquisitionName);
    
    if (!jobAcquisition.getRegisteredProcessEngines().isEmpty()) {
      List<ProcessEngineConfigurationImpl> registeredProcessEngines = jobAcquisition.getRegisteredProcessEngines();
      StringBuffer sb = new StringBuffer("[");
      for (ProcessEngineConfigurationImpl peci : registeredProcessEngines) {
        sb.append(peci.getProcessEngineName() + ", ");
      }
      sb.append("]");
      int lastIndexOf = sb.lastIndexOf(",");
      sb.deleteCharAt(lastIndexOf).deleteCharAt(lastIndexOf+1);
      throw new ProcessEngineException("Unable to remove jobAcquisition '" + jobAcquisitionName + "' because following process engines are still registered with it: " + sb.toString());
    }
    service.stopJobAcquisition(jobAcquisitionName);
  }

}
