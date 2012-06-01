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

import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_DEFAULT;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_NAME;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ELEMENT_DATASOURCE;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ELEMENT_HISTORY_LEVEL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Locale;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformParser.Tag;

/**
 * Defines the fox-platform subsystem for jboss application server
 * 
 * @author Daniel Meyer
 */
public class FoxPlatformExtension implements Extension {

  /** The name space used for the {@code subsystem} element */
  public static final String NAMESPACE = "urn:com.camunda.fox.fox-platform:1.0";

  /** The name of our subsystem within the model. */
  public static final String SUBSYSTEM_NAME = "fox-platform";
  
  /** The parser used for parsing our subsystem */
  private final FoxPlatformParser parser = new FoxPlatformParser();

  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, /* TODO: use versioning of the management interface?*/ 1, 0);
    ManagementResourceRegistration subsystemModel = subsystem.registerSubsystemModel(ModelDescriptionProviders.SUBSYSTEM);
    subsystemModel.registerOperationHandler(ADD, SubsystemAddHandler.INSTANCE, ModelDescriptionProviders.SUBSYSTEM_ADD, false);
    subsystemModel.registerOperationHandler(DESCRIBE, SubsystemDescribeHandler.INSTANCE, SubsystemDescribeHandler.INSTANCE, false,OperationEntry.EntryType.PRIVATE);
    
//    ManagementResourceRegistration processEnginesChild = subsystemModel.registerSubModel(PathElement.pathElement("process-engines"), ModelDescriptionProviders.PROCESS_ENGINES_DEC);
    
    // Add the process-engine child and operation handlers
    ManagementResourceRegistration processEngineChild = subsystemModel.registerSubModel(PathElement.pathElement("process-engine"), ModelDescriptionProviders.PROCESS_ENGINE_DEC);
    processEngineChild.registerOperationHandler(ModelDescriptionConstants.ADD, ProcessEngineAddHandler.INSTANCE, ProcessEngineAddHandler.INSTANCE);
    processEngineChild.registerOperationHandler(ModelDescriptionConstants.REMOVE, ProcessEngineRemoveHandler.INSTANCE, ProcessEngineRemoveHandler.INSTANCE);
    
    // THINK: here we could add handlers for additional read-write attributes. They would react to a change in the model. 
    // A process engine is mostly read only. However, I could imagine values like the locktime of the jobexecutor to be configurable here.
    // a change to such a value through one of the management interfaces would be persisted in the configuration (Storage.CONFIGURATION) 
    // and distributed across a cluster / domain
    // Example: processEngineChild.registerReadWriteAttribute("jobExecututorLockTime", null, JobExecutorLockTimeHandler.INSTANCE, Storage.CONFIGURATION);

    subsystem.registerXMLElementWriter(parser);

  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
  }

  public static ModelNode createAddSubsystemOperation() {
    final ModelNode subsystem = new ModelNode();
    subsystem.get(OP).set(ADD);
    subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
    return subsystem;
  }

  /**
   * Recreate the steps to put the subsystem in the same state it was in. This
   * is used in domain mode to query the profile being used, in order to get the
   * steps needed to create the servers
   */
  private static class SubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {

    static final SubsystemDescribeHandler INSTANCE = new SubsystemDescribeHandler();

    public ModelNode getModelDescription(Locale locale) {
      return CommonDescriptions.getSubsystemDescribeOperation(locale);
    }

    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
      // Add the main operation
      context.getResult().add(createAddSubsystemOperation());
      
      //Add the operations to create each child
      ModelNode node = context.readResource(PathAddress.EMPTY_ADDRESS).getModel();
      for (Property property : node.get(Tag.PROCESS_ENGINE.getLocalName()).asPropertyList()) {
          ModelNode addType = new ModelNode();
          addType.get(OP).set(ModelDescriptionConstants.ADD);
          PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement(Tag.PROCESS_ENGINES.getLocalName()), PathElement.pathElement(Tag.PROCESS_ENGINE.getLocalName(), property.getName()));
          addType.get(OP_ADDR).set(addr.toModelNode());
          addType.get(ATTR_NAME).set(property.getValue().get(ATTR_NAME).asString());
          if (property.getValue().hasDefined(ATTR_DEFAULT)) {
            addType.get(ATTR_DEFAULT).set(property.getValue().get(ATTR_DEFAULT).asString());
          }
          if (property.getValue().hasDefined(ELEMENT_DATASOURCE)) {
              addType.get(ELEMENT_DATASOURCE).set(property.getValue().get(ELEMENT_DATASOURCE).asString());
          }
          if (property.getValue().hasDefined(ELEMENT_HISTORY_LEVEL)) {
            addType.get(ELEMENT_HISTORY_LEVEL).set(property.getValue().get(ELEMENT_HISTORY_LEVEL).asString());
          }
          context.getResult().add(addType);
      }
      context.completeStep();
    }

  }

}
