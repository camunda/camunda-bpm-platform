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

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.ACQUISITION_STRATEGY;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.NAME;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import java.util.List;
import java.util.Locale;

import org.camunda.bpm.container.impl.jboss.service.MscRuntimeContainerJobExecutor;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.engine.impl.jobexecutor.RuntimeContainerJobExecutor;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;


/**
 * Provides the description and the implementation of the job-acquisition#add operation.
 * 
 */
public class JobAcquisitionAdd extends AbstractAddStepHandler implements DescriptionProvider {
    
  public static final JobAcquisitionAdd INSTANCE = new JobAcquisitionAdd();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a job acquisition");
    node.get(OPERATION_NAME).set(ADD);
    
    node.get(REQUEST_PROPERTIES, NAME, DESCRIPTION).set("Name of job acquisition thread");
    node.get(REQUEST_PROPERTIES, NAME, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, NAME, REQUIRED).set(true);
    
    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, DESCRIPTION).set("Job acquisition strategy");
    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, REQUIRED).set(false);
        
    node.get(REQUEST_PROPERTIES, PROPERTIES, DESCRIPTION).set("Additional properties");
    node.get(REQUEST_PROPERTIES, PROPERTIES, TYPE).set(ModelType.OBJECT);
    node.get(REQUEST_PROPERTIES, PROPERTIES, VALUE_TYPE).set(ModelType.LIST);
    node.get(REQUEST_PROPERTIES, PROPERTIES, REQUIRED).set(false);

    return node;
  }

  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    String name = "default";
    if (operation.hasDefined(NAME)) {
      name = operation.get(NAME).asString();
    }
    model.get(NAME).set(name);
    
    String acquisitionStrategy = "SEQUENTIAL";
    if (operation.hasDefined(ACQUISITION_STRATEGY)) {
      acquisitionStrategy = operation.get(ACQUISITION_STRATEGY).asString();
    }
    model.get(ACQUISITION_STRATEGY).set(acquisitionStrategy);
        
    // retrieve all properties
    ModelNode properties = new ModelNode();
    if (operation.hasDefined(PROPERTIES)) {
      properties = operation.get(PROPERTIES).asObject();
    }
    model.get(PROPERTIES).set(properties);
  }
  
  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {
       
    String acquisitionName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    
    MscRuntimeContainerJobExecutor mscRuntimeContainerJobExecutor = new MscRuntimeContainerJobExecutor();
  
    // start new service for job executor
    ServiceController<RuntimeContainerJobExecutor> serviceController = context.getServiceTarget().addService(ServiceNames.forMscRuntimeContainerJobExecutorService(acquisitionName), mscRuntimeContainerJobExecutor)
      .addDependency(ServiceNames.forMscRuntimeContainerDelegate())
      .addDependency(ServiceNames.forMscExecutorService())
      .addListener(verificationHandler)
      .setInitialMode(Mode.ACTIVE)
      .install();
    
    newControllers.add(serviceController);
    
  }

}
