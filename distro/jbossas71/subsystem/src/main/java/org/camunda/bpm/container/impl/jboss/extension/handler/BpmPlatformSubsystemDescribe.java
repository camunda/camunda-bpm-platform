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
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.DATASOURCE;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.DEFAULT;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.HISTORY_LEVEL;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.JOB_ACQUISITIONS;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.JOB_EXECUTOR;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.NAME;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.PROCESS_ENGINES;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.PROPERTIES;
import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.THREAD_POOL_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 * Recreate the steps to put the subsystem in the same state it was in. This
 * is used in domain mode to query the profile being used, in order to get the
 * steps needed to create the servers
 */
public class BpmPlatformSubsystemDescribe implements OperationStepHandler, DescriptionProvider {

  public static final BpmPlatformSubsystemDescribe INSTANCE = new BpmPlatformSubsystemDescribe();

  /** {@inheritDoc} */
  @Override
  public ModelNode getModelDescription(Locale locale) {
    return CommonDescriptions.getSubsystemDescribeOperation(locale);
  }

  /** {@inheritDoc} */
  @Override
  public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
    final Resource resource = context.readResource(PathAddress.EMPTY_ADDRESS);
    final ModelNode subModel = Resource.Tools.readModel(resource);
    
    final ModelNode subsystemAdd = new ModelNode();
    PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(OP_ADDR)).getLastElement());
    subsystemAdd.get(OP).set(ADD);
    subsystemAdd.get(OP_ADDR).set(rootAddress.toModelNode());
    
    // Add the main operation
    ModelNode result = context.getResult();
    result.add(subsystemAdd);
    
    //Add the operations to create each child
    if (subModel.hasDefined(PROCESS_ENGINES)) {
      for (Property property : subModel.get(PROCESS_ENGINES).asPropertyList()) {
        ModelNode processEngineAdd = new ModelNode();
        processEngineAdd.get(OP).set(ADD);
        PathAddress processEngineAddress = rootAddress.append(PathElement.pathElement(PROCESS_ENGINES, property.getName()));
        processEngineAdd.get(OP_ADDR).set(processEngineAddress.toModelNode());
        addProcessEngine(property.getValue(), processEngineAdd, processEngineAddress, result);
      }
    }
    
    if (subModel.hasDefined(JOB_EXECUTOR)) {
      ModelNode jobExecutorModel = subModel.get(JOB_EXECUTOR);
      for (Property property : jobExecutorModel.asPropertyList()) {
        ModelNode thisJobExecutorModelNode = property.getValue();
        ModelNode jobExecutorAdd = new ModelNode();
        jobExecutorAdd.get(OP).set(ADD);
        PathAddress jobExecutorAddress = rootAddress.append(PathElement.pathElement(JOB_EXECUTOR, property.getName()));
        jobExecutorAdd.get(OP_ADDR).set(jobExecutorAddress.toModelNode());
        addJobExecutor(property.getValue(), jobExecutorAdd, jobExecutorAddress, result);
        
        if (thisJobExecutorModelNode.hasDefined(JOB_ACQUISITIONS)) {
          for (Property jobAcquisitionProperty : thisJobExecutorModelNode.get(JOB_ACQUISITIONS).asPropertyList()) {
            ModelNode jobAcquisitionAdd = new ModelNode();
            jobAcquisitionAdd.get(OP).set(ADD);
            PathAddress jobAcquisitionAddress = jobExecutorAddress.append(PathElement.pathElement(JOB_ACQUISITIONS, jobAcquisitionProperty.getName()));
            jobAcquisitionAdd.get(OP_ADDR).set(jobAcquisitionAddress.toModelNode());
            addJobAcquisition(jobAcquisitionProperty.getValue(), jobAcquisitionAdd, jobAcquisitionAddress, result);
          }
        }
        
      }
    }
    
    context.completeStep();
  }

  private void addProcessEngine(ModelNode property, ModelNode processEngineAdd, PathAddress processEngineAddress, ModelNode result) {
    processEngineAdd.get(NAME).set(property.get(NAME).asString());
    if (property.hasDefined(DATASOURCE)) {
      processEngineAdd.get(DATASOURCE).set(property.get(DATASOURCE).asString());
    }
    if (property.hasDefined(DEFAULT)) {
      processEngineAdd.get(DEFAULT).set(property.get(DEFAULT).asBoolean());
    }
    if (property.hasDefined(HISTORY_LEVEL)) {
      processEngineAdd.get(HISTORY_LEVEL).set(property.get(HISTORY_LEVEL).asString());
    }
    if (property.hasDefined(PROPERTIES)) {
      processEngineAdd.get(PROPERTIES).set(property.get(PROPERTIES).asList());
    }
    
    result.add(processEngineAdd);
  }
  
  private void addJobExecutor(ModelNode property, ModelNode jobExecutorAdd, PathAddress jobExecutorAddress, ModelNode result) {
    jobExecutorAdd.get(THREAD_POOL_NAME).set(property.get(THREAD_POOL_NAME).asString());
    
    result.add(jobExecutorAdd);
  }
  
  private void addJobAcquisition(ModelNode property, ModelNode jobAcquisitionAdd, PathAddress jobAcquisitionAddress, ModelNode result) {
    jobAcquisitionAdd.get(NAME).set(property.get(NAME).asString());
    
    if (property.hasDefined(ACQUISITION_STRATEGY)) {
      jobAcquisitionAdd.get(ACQUISITION_STRATEGY).set(property.get(ACQUISITION_STRATEGY).asString());
    }
    
    if (property.hasDefined(PROPERTIES)) {
      jobAcquisitionAdd.get(PROPERTIES).set(property.get(PROPERTIES).asList());
    }
    
    result.add(jobAcquisitionAdd);
  }
  
}
