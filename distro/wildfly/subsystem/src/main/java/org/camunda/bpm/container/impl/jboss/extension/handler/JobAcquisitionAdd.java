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

import java.util.List;

import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.service.MscRuntimeContainerJobExecutor;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.engine.impl.jobexecutor.RuntimeContainerJobExecutor;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;


/**
 * Provides the description and the implementation of the job-acquisition#add operation.
 *
 * @author Christian Lipphardt
 */
public class JobAcquisitionAdd extends AbstractAddStepHandler {

  public static final JobAcquisitionAdd INSTANCE = new JobAcquisitionAdd();

  @Override
  protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

    String acquisitionName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();

    MscRuntimeContainerJobExecutor mscRuntimeContainerJobExecutor = new MscRuntimeContainerJobExecutor();

    if (model.hasDefined(SubsystemAttributeDefinitons.PROPERTIES.getName())) {
      List<Property> properties = SubsystemAttributeDefinitons.PROPERTIES.resolveModelAttribute(context, model).asPropertyList();
      for (Property property : properties) {
        PropertyHelper.applyProperty(mscRuntimeContainerJobExecutor, property.getName(), property.getValue().asString());
      }
    }

    // start new service for job executor
    ServiceBuilder<RuntimeContainerJobExecutor> serviceBuilder = context.getCapabilityServiceTarget().addService(ServiceNames.forMscRuntimeContainerJobExecutorService(acquisitionName), mscRuntimeContainerJobExecutor)
        .setInitialMode(Mode.ACTIVE);
    serviceBuilder.requires(ServiceNames.forMscRuntimeContainerDelegate());
    serviceBuilder.requires(ServiceNames.forMscExecutorService());
    serviceBuilder.install();
  }

//  @Override
//  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
  //There is operation validateAndSet on AttributeDefinition that helps us validate and set the model based on definition of the attribute.
//      TypeDefinition.TICK.validateAndSet(operation,model);
  //...
//  }


}
