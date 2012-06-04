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
