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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;

public class MySubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

  static final String NAME = "camunda";
  static final PathElement PATH = SubsystemResourceDefinitionRegistrar.pathElement(NAME);
  static final ParentResourceDescriptionResolver RESOLVER = new SubsystemResourceDescriptionResolver(NAME, MySubsystemRegistrar.class);

  @Override
  public ManagementResourceRegistration register(SubsystemRegistration parent, ManagementResourceRegistrationContext context) {
      parent.setHostCapable();
      ManagementResourceRegistration registration = parent.registerSubsystemModel(BpmPlatformRootDefinition.INSTANCE);
      ResourceDescriptor descriptor = ResourceDescriptor.builder(RESOLVER).build();
      ManagementResourceRegistrar.of(descriptor).register(registration);
      return registration;
  }
}
