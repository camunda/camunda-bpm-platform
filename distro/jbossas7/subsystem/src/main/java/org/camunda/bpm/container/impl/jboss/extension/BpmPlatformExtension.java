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
package org.camunda.bpm.container.impl.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.camunda.bpm.container.impl.jboss.extension.handler.BpmPlatformSubsystemDescribe;
import org.camunda.bpm.container.impl.jboss.extension.resource.BpmPlatformSubsystemRootResourceDefinition;
import org.camunda.bpm.container.impl.jboss.extension.resource.JobAcquisitionResourceDefinition;
import org.camunda.bpm.container.impl.jboss.extension.resource.JobExecutorResourceDefinition;
import org.camunda.bpm.container.impl.jboss.extension.resource.ProcessEnginesResourceDefinition;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.msc.service.ServiceName;


/**
 * Defines the bpm-platform subsystem for jboss application server
 * 
 * @author Daniel Meyer
 */
public class BpmPlatformExtension implements Extension {

  public static final int FOX_PLATFORM_SUBSYSTEM_MAJOR_VERSION = 1;
  public static final int FOX_PLATFORM_SUBSYSTEM_MINOR_VERSION = 1;
  
  /** The parser used for parsing our subsystem */
  private final BpmPlatformParser parser = new BpmPlatformParser();
  
  public static final String RESOURCE_NAME = BpmPlatformExtension.class.getPackage().getName() + ".LocalDescriptions";

  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(ModelConstants.SUBSYSTEM_NAME, FOX_PLATFORM_SUBSYSTEM_MAJOR_VERSION, FOX_PLATFORM_SUBSYSTEM_MINOR_VERSION);
    subsystem.registerXMLElementWriter(parser);
    
    // Root resource
    final ManagementResourceRegistration rootRegistration = subsystem.registerSubsystemModel(BpmPlatformSubsystemRootResourceDefinition.INSTANCE);
    rootRegistration.registerOperationHandler(DESCRIBE, BpmPlatformSubsystemDescribe.INSTANCE, BpmPlatformSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
    
    // Process Engine
    rootRegistration.registerSubModel(new ProcessEnginesResourceDefinition());
    
    // Job Executor
    ManagementResourceRegistration jobExecutorRegistration = rootRegistration.registerSubModel(new JobExecutorResourceDefinition());
    //Job acquisition
    jobExecutorRegistration.registerSubModel(new JobAcquisitionResourceDefinition());
    
  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.CAMUNDA_BPM_PLATFORM_1_1.getUriString(), parser);
  }

  public static ServiceName getPlatformServiceType() {
    return ServiceName.of("org").append("camunda").append("bpm").append("platform");
  }

  public static ResourceDescriptionResolver getResourceDescriptionResolver(String keyPrefix) {
    return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, BpmPlatformExtension.class.getClassLoader(), true, true);
  }

}
