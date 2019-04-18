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
package org.camunda.bpm.container.impl.jboss.extension;

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.SUBSYSTEM_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.camunda.bpm.container.impl.jboss.extension.handler.BpmPlatformSubsystemAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.BpmPlatformSubsystemRemove;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionRemove;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobExecutorAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobExecutorRemove;
import org.camunda.bpm.container.impl.jboss.extension.handler.ProcessEngineAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.ProcessEngineRemove;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceBuilder;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;



/**
 * Defines the bpm-platform subsystem for jboss application server
 *
 * @author Daniel Meyer
 */
public class BpmPlatformExtension implements Extension {

  public static final int BPM_PLATFORM_SUBSYSTEM_MAJOR_VERSION = 1;
  public static final int BPM_PLATFORM_SUBSYSTEM_MINOR_VERSION = 1;

  /** The parser used for parsing our subsystem */
  private final BpmPlatformParser parser = new BpmPlatformParser();

  public static final String RESOURCE_NAME = BpmPlatformExtension.class.getPackage().getName() + ".LocalDescriptions";

  private static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
  private static final PathElement PROCESS_ENGINES_PATH  = PathElement.pathElement(ModelConstants.PROCESS_ENGINES);
  private static final PathElement JOB_EXECUTOR_PATH  = PathElement.pathElement(ModelConstants.JOB_EXECUTOR);
  private static final PathElement JOB_ACQUISTIONS_PATH  = PathElement.pathElement(ModelConstants.JOB_ACQUISITIONS);


  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, BPM_PLATFORM_SUBSYSTEM_MAJOR_VERSION, BPM_PLATFORM_SUBSYSTEM_MINOR_VERSION);
    subsystem.registerXMLElementWriter(parser);

    // build resource definitions

    ResourceBuilder processEnginesResource = ResourceBuilder.Factory.create(PROCESS_ENGINES_PATH, getResourceDescriptionResolver(ModelConstants.PROCESS_ENGINES))
      .setAddOperation(ProcessEngineAdd.INSTANCE)
      .setRemoveOperation(ProcessEngineRemove.INSTANCE);

    ResourceBuilder jobAcquisitionResource = ResourceBuilder.Factory.create(JOB_ACQUISTIONS_PATH, getResourceDescriptionResolver(ModelConstants.JOB_ACQUISITIONS))
        .setAddOperation(JobAcquisitionAdd.INSTANCE)
        .setRemoveOperation(JobAcquisitionRemove.INSTANCE);

    ResourceBuilder jobExecutorResource = ResourceBuilder.Factory.create(JOB_EXECUTOR_PATH, getResourceDescriptionResolver(ModelConstants.JOB_EXECUTOR))
      .setAddOperation(JobExecutorAdd.INSTANCE)
      .setRemoveOperation(JobExecutorRemove.INSTANCE)
      .pushChild(jobAcquisitionResource).pop();

    ResourceDefinition subsystemResource = ResourceBuilder.Factory.createSubsystemRoot(SUBSYSTEM_PATH, getResourceDescriptionResolver(SUBSYSTEM_NAME), BpmPlatformSubsystemAdd.INSTANCE, BpmPlatformSubsystemRemove.INSTANCE)
      .pushChild(processEnginesResource).pop()
      .pushChild(jobExecutorResource).pop()
      .build();

    subsystem.registerSubsystemModel(subsystemResource);

  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.CAMUNDA_BPM_PLATFORM_1_1.getUriString(), parser);
  }

  public static StandardResourceDescriptionResolver getResourceDescriptionResolver(String keyPrefix) {
    return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, BpmPlatformExtension.class.getClassLoader(), true, true);
  }

}
