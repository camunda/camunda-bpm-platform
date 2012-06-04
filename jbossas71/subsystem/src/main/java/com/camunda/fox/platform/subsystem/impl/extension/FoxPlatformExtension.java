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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

import com.camunda.fox.platform.subsystem.impl.extension.handler.FoxPlatformSubsystemDescribe;
import com.camunda.fox.platform.subsystem.impl.extension.resource.FoxPlatformSubsystemRootResourceDefinition;
import com.camunda.fox.platform.subsystem.impl.extension.resource.ProcessEnginesResourceDefinition;

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
  
  private static final String RESOURCE_NAME = FoxPlatformExtension.class.getPackage().getName() + ".LocalDescriptions";

  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
    subsystem.registerXMLElementWriter(parser);
    
    // Root resource
    final ManagementResourceRegistration rootRegistration = subsystem.registerSubsystemModel(FoxPlatformSubsystemRootResourceDefinition.INSTANCE);
    rootRegistration.registerOperationHandler(DESCRIBE, FoxPlatformSubsystemDescribe.INSTANCE, FoxPlatformSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
    
    final ManagementResourceRegistration processEnginesRegistration = rootRegistration.registerSubModel(new ProcessEnginesResourceDefinition());
    
    // TODO: Job executor
//    ManagementResourceRegistration jobExecutorChild = rootRegistration.registerSubModel(PathElement.pathElement("job-executor"));
//    jobExecutorChild.registerOperationHandler(ModelDescriptionConstants.ADD, JobExecutorAddHandler.INSTANCE, JobExecutorAddHandler.INSTANCE);
    
    // THINK: here we could add handlers for additional read-write attributes. They would react to a change in the model. 
    // A process engine is mostly read only. However, I could imagine values like the locktime of the jobexecutor to be configurable here.
    // a change to such a value through one of the management interfaces would be persisted in the configuration (Storage.CONFIGURATION) 
    // and distributed across a cluster / domain
    // Example: processEngineChild.registerReadWriteAttribute("jobExecututorLockTime", null, JobExecutorLockTimeHandler.INSTANCE, Storage.CONFIGURATION);
  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
  }

  public static ResourceDescriptionResolver getResourceDescriptionResolver(String keyPrefix) {
    return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, FoxPlatformExtension.class.getClassLoader(), true, true);
  }

}
