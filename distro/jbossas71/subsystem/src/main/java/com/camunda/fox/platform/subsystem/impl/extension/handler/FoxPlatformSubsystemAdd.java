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
package com.camunda.fox.platform.subsystem.impl.extension.handler;

import java.util.List;

import org.camunda.bpm.ProcessEngineService;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

import com.camunda.fox.platform.subsystem.impl.deployment.processor.ModuleDependencyProcessor;
import com.camunda.fox.platform.subsystem.impl.deployment.processor.ProcessEngineClientProcessor;
import com.camunda.fox.platform.subsystem.impl.deployment.processor.ProcessEngineDependencyProcessor;
import com.camunda.fox.platform.subsystem.impl.deployment.processor.ProcessEngineDeploymentProcessor;
import com.camunda.fox.platform.subsystem.impl.deployment.processor.ProcessesXmlProcessor;
import com.camunda.fox.platform.subsystem.impl.extension.ModelConstants;
import com.camunda.fox.platform.subsystem.impl.service.ContainerProcessEngineService;

/**
 * Provides the description and the implementation of the subsystem#add operation.
 * 
 * @author Daniel Meyer
 */
public class FoxPlatformSubsystemAdd extends AbstractBoottimeAddStepHandler {
  
  public static final FoxPlatformSubsystemAdd INSTANCE = new FoxPlatformSubsystemAdd();
  
  private FoxPlatformSubsystemAdd() {
  }
  
  /** {@inheritDoc} */
  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    model.get(ModelConstants.PROCESS_ENGINES);
  }
  
  /** {@inheritDoc} */
  @Override
  protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
          List<ServiceController< ? >> newControllers) throws OperationFailedException {
    
    // add deployment processors
    context.addStep(new AbstractDeploymentChainStep() {
      public void execute(DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.PARSE, ProcessesXmlProcessor.PRIORITY, new ProcessesXmlProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.PARSE, ProcessEngineClientProcessor.PRIORITY, new ProcessEngineClientProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.DEPENDENCIES, ModuleDependencyProcessor.PRIORITY, new ModuleDependencyProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.DEPENDENCIES, ProcessEngineDependencyProcessor.PRIORITY, new ProcessEngineDependencyProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.INSTALL, ProcessEngineDeploymentProcessor.PRIORITY, new ProcessEngineDeploymentProcessor());
      }
    }, OperationContext.Stage.RUNTIME);

    // create and register DefaultProcessEngineService
    final ContainerProcessEngineService processEngineService = new ContainerProcessEngineService();
    
    final ServiceController<ProcessEngineService> controller = context.getServiceTarget()           
            .addService(ContainerProcessEngineService.getServiceName(), processEngineService)
            .addListener(verificationHandler)
            .setInitialMode(Mode.ACTIVE)
            .install();
    
    newControllers.add(controller);
  }

}
