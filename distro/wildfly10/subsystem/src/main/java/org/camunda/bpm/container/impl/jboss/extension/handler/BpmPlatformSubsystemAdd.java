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

import java.util.List;

import org.camunda.bpm.container.impl.jboss.deployment.processor.ModuleDependencyProcessor;
import org.camunda.bpm.container.impl.jboss.deployment.processor.ProcessApplicationDeploymentProcessor;
import org.camunda.bpm.container.impl.jboss.deployment.processor.ProcessApplicationProcessor;
import org.camunda.bpm.container.impl.jboss.deployment.processor.ProcessEngineStartProcessor;
import org.camunda.bpm.container.impl.jboss.deployment.processor.ProcessesXmlProcessor;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.service.MscBpmPlatformPlugins;
import org.camunda.bpm.container.impl.jboss.service.MscRuntimeContainerDelegate;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
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


/**
 * Provides the description and the implementation of the subsystem#add operation.
 *
 * @author Daniel Meyer
 */
public class BpmPlatformSubsystemAdd extends AbstractBoottimeAddStepHandler {

  public static final BpmPlatformSubsystemAdd INSTANCE = new BpmPlatformSubsystemAdd();

  private BpmPlatformSubsystemAdd() {
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
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.PARSE, ProcessApplicationProcessor.PRIORITY, new ProcessApplicationProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.DEPENDENCIES, ModuleDependencyProcessor.PRIORITY, new ModuleDependencyProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.POST_MODULE, ProcessesXmlProcessor.PRIORITY, new ProcessesXmlProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.INSTALL, ProcessEngineStartProcessor.PRIORITY, new ProcessEngineStartProcessor());
        processorTarget.addDeploymentProcessor(ModelConstants.SUBSYSTEM_NAME, Phase.INSTALL, ProcessApplicationDeploymentProcessor.PRIORITY, new ProcessApplicationDeploymentProcessor());
      }
    }, OperationContext.Stage.RUNTIME);

    // create and register the MSC container delegate.
    final MscRuntimeContainerDelegate processEngineService = new MscRuntimeContainerDelegate();

    final ServiceController<MscRuntimeContainerDelegate> controller = context.getServiceTarget()
            .addService(ServiceNames.forMscRuntimeContainerDelegate(), processEngineService)
            .addListener(verificationHandler)
            .setInitialMode(Mode.ACTIVE)
            .install();

    newControllers.add(controller);

    // discover and register bpm platform plugins
    BpmPlatformPlugins plugins = BpmPlatformPlugins.load(getClass().getClassLoader());
    MscBpmPlatformPlugins managedPlugins = new MscBpmPlatformPlugins(plugins);

    ServiceController<BpmPlatformPlugins> serviceController = context.getServiceTarget()
      .addService(ServiceNames.forBpmPlatformPlugins(), managedPlugins)
      .addListener(verificationHandler)
      .setInitialMode(Mode.ACTIVE)
      .install();

    newControllers.add(serviceController);
  }

}
