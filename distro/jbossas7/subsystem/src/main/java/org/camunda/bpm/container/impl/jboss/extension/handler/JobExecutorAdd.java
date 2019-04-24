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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.THREAD_POOL_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.List;
import java.util.Locale;

import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.service.MscExecutorService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.engine.ProcessEngineException;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.as.threads.ThreadsServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;


/**
 * Installs the JobExecutor service into the container.
 * 
 */
public class JobExecutorAdd extends AbstractAddStepHandler implements DescriptionProvider {
    
  public static final JobExecutorAdd INSTANCE = new JobExecutorAdd();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a job executor");
    node.get(OPERATION_NAME).set(ADD);
    
    node.get(REQUEST_PROPERTIES, THREAD_POOL_NAME, DESCRIPTION).set("Thread pool name for global job executor");
    node.get(REQUEST_PROPERTIES, THREAD_POOL_NAME, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, THREAD_POOL_NAME, REQUIRED).set(true);
    
    return node;
  }

  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    for (AttributeDefinition attr : SubsystemAttributeDefinitons.JOB_EXECUTOR_ATTRIBUTES) {
      attr.validateAndSet(operation, model);
    }
  }
  
  
  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {
    
    if (!operation.hasDefined(THREAD_POOL_NAME)) {
      throw new ProcessEngineException("Unable to configure threadpool for ContainerJobExecutorService, missing element '" + THREAD_POOL_NAME + "' in JobExecutor configuration.");
    }
    
    String jobExecutorThreadPoolName = SubsystemAttributeDefinitons.THREAD_POOL_NAME.resolveModelAttribute(context, model).asString();

    MscExecutorService service = new MscExecutorService();
    ServiceController<MscExecutorService> serviceController = context.getServiceTarget().addService(ServiceNames.forMscExecutorService(), service)
        .addDependency(ThreadsServices.EXECUTOR.append(jobExecutorThreadPoolName), ManagedQueueExecutorService.class, service.getManagedQueueInjector())
        .addListener(verificationHandler)
        .setInitialMode(Mode.ACTIVE)
        .install();
    
    newControllers.add(serviceController);
    
  }

}
