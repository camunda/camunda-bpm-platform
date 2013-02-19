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
import com.camunda.fox.platform.subsystem.impl.extension.resource.JobAcquisitionResourceDefinition;
import com.camunda.fox.platform.subsystem.impl.extension.resource.JobExecutorResourceDefinition;
import com.camunda.fox.platform.subsystem.impl.extension.resource.ProcessEnginesResourceDefinition;

/**
 * Defines the fox-platform subsystem for jboss application server
 * 
 * @author Daniel Meyer
 */
public class FoxPlatformExtension implements Extension {

  public static final int FOX_PLATFORM_SUBSYSTEM_MAJOR_VERSION = 1;
  public static final int FOX_PLATFORM_SUBSYSTEM_MINOR_VERSION = 1;
  
  /** The parser used for parsing our subsystem */
  private final FoxPlatformParser parser = new FoxPlatformParser();
  
  private static final String RESOURCE_NAME = FoxPlatformExtension.class.getPackage().getName() + ".LocalDescriptions";

  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(ModelConstants.SUBSYSTEM_NAME, FOX_PLATFORM_SUBSYSTEM_MAJOR_VERSION, FOX_PLATFORM_SUBSYSTEM_MINOR_VERSION);
    subsystem.registerXMLElementWriter(parser);
    
    // Root resource
    final ManagementResourceRegistration rootRegistration = subsystem.registerSubsystemModel(FoxPlatformSubsystemRootResourceDefinition.INSTANCE);
    rootRegistration.registerOperationHandler(DESCRIBE, FoxPlatformSubsystemDescribe.INSTANCE, FoxPlatformSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
    
    // Process Engine
    rootRegistration.registerSubModel(new ProcessEnginesResourceDefinition());
    
    // Job Executor
    ManagementResourceRegistration jobExecutorRegistration = rootRegistration.registerSubModel(new JobExecutorResourceDefinition());
    //Job acquisition
    jobExecutorRegistration.registerSubModel(new JobAcquisitionResourceDefinition());
    
  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.FOX_PLATFORM_1_0.getUriString(), parser);
    context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.FOX_PLATFORM_1_1.getUriString(), parser);
  }

  public static ResourceDescriptionResolver getResourceDescriptionResolver(String keyPrefix) {
    return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, FoxPlatformExtension.class.getClassLoader(), true, true);
  }

}
