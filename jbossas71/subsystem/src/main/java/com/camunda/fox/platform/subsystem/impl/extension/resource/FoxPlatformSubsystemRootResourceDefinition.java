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
package com.camunda.fox.platform.subsystem.impl.extension.resource;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;
import com.camunda.fox.platform.subsystem.impl.extension.handler.FoxPlatformSubsystemAdd;


public class FoxPlatformSubsystemRootResourceDefinition extends SimpleResourceDefinition {

  private static final PathElement SUBSYSTEM_PATH  = PathElement.pathElement(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME);

  public static final FoxPlatformSubsystemRootResourceDefinition INSTANCE = new FoxPlatformSubsystemRootResourceDefinition();

  private FoxPlatformSubsystemRootResourceDefinition() {
      super(SUBSYSTEM_PATH, FoxPlatformExtension.getResourceDescriptionResolver(FoxPlatformExtension.SUBSYSTEM_NAME),
              FoxPlatformSubsystemAdd.INSTANCE, ReloadRequiredRemoveStepHandler.INSTANCE);
  }
  
}
