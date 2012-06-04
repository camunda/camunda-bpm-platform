package com.camunda.fox.platform.subsystem.impl.extension.resource;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;
import com.camunda.fox.platform.subsystem.impl.extension.ModelConstants;
import com.camunda.fox.platform.subsystem.impl.extension.handler.ProcessEngineAdd;
import com.camunda.fox.platform.subsystem.impl.extension.handler.ProcessEngineRemove;


public class ProcessEnginesResourceDefinition extends SimpleResourceDefinition {

  private static final PathElement PROCESS_ENGINES_PATH  = PathElement.pathElement(ModelConstants.PROCESS_ENGINES);
  
  public ProcessEnginesResourceDefinition() {
    super(PROCESS_ENGINES_PATH, FoxPlatformExtension.getResourceDescriptionResolver(ModelConstants.PROCESS_ENGINES),
            ProcessEngineAdd.INSTANCE, ProcessEngineRemove.INSTANCE);
  }
  
}
