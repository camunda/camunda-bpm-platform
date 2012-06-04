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

import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.*;
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
public class FoxPlatformSubsystemDescribe implements OperationStepHandler, DescriptionProvider {

  public static final FoxPlatformSubsystemDescribe INSTANCE = new FoxPlatformSubsystemDescribe();

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
  
}
