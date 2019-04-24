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

import org.camunda.bpm.container.impl.jboss.extension.resource.BpmPlatformRootDefinition;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.SUBSYSTEM_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;



/**
 * Defines the bpm-platform subsystem for Wildfly 8+ application server
 *
 * @author Daniel Meyer
 */
public class BpmPlatformExtension implements Extension {

  public static final int BPM_PLATFORM_SUBSYSTEM_MAJOR_VERSION = 1;
  public static final int BPM_PLATFORM_SUBSYSTEM_MINOR_VERSION = 1;

  /** The parser used for parsing our subsystem */
  private final BpmPlatformParser1_1.BpmPlatformSubsystemParser parser = BpmPlatformParser1_1.BpmPlatformSubsystemParser.INSTANCE;

  public static final String RESOURCE_NAME = BpmPlatformExtension.class.getPackage().getName() + ".LocalDescriptions";

  /**
   * Path elements for the resources offered by the subsystem.
   */
  public static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
  public static final PathElement PROCESS_ENGINES_PATH = PathElement.pathElement(ModelConstants.PROCESS_ENGINES);
  public static final PathElement JOB_EXECUTOR_PATH = PathElement.pathElement(ModelConstants.JOB_EXECUTOR);
  public static final PathElement JOB_ACQUISTIONS_PATH = PathElement.pathElement(ModelConstants.JOB_ACQUISITIONS);

  @Override
  public void initialize(ExtensionContext context) {
    SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, BPM_PLATFORM_SUBSYSTEM_MAJOR_VERSION, BPM_PLATFORM_SUBSYSTEM_MINOR_VERSION);
    subsystem.registerSubsystemModel(BpmPlatformRootDefinition.INSTANCE);
    subsystem.registerXMLElementWriter(parser);
  }

  @Override
  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.CAMUNDA_BPM_PLATFORM_1_1.getUriString(), parser);
  }

  /**
   * Resolve the descriptions of the resources from the 'LocalDescriptions.properties' file.
   */
  public static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String... keyPrefix) {
    StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
    for (String kp : keyPrefix) {
      prefix.append('.').append(kp);
    }
    return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, BpmPlatformExtension.class.getClassLoader(), true, false);
  }

}
