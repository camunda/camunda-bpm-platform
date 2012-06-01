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
package com.camunda.fox.platform.subsystem.impl.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformParser.Tag;
import com.camunda.fox.platform.subsystem.impl.platform.ContainerPlatformService;

/**
 * Provides the description and the implementation of the subsystem#add operation.
 * 
 * @author Daniel Meyer
 */
public class SubsystemAddHandler extends AbstractBoottimeAddStepHandler {
  
  static final SubsystemAddHandler INSTANCE = new SubsystemAddHandler();
  
  /** {@inheritDoc} */
  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    model.get(Tag.PROCESS_ENGINES.getLocalName()).setEmptyObject();
  }
  
  /** {@inheritDoc} */
  @Override
  protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
          List<ServiceController< ? >> newControllers) throws OperationFailedException {
    
    final ContainerPlatformService containerPlatformService = new ContainerPlatformService();
    
    final ServiceController<ContainerPlatformService> controller = context.getServiceTarget()           
            .addService(ContainerPlatformService.getServiceName(), containerPlatformService)        
            .addListener(verificationHandler)
            .setInitialMode(Mode.ACTIVE)
            .install();
    
    newControllers.add(controller);
    
  }

}
