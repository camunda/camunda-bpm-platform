package org.camunda.bpm.container.impl.jboss.extension.resource;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.extension.handler.ProcessEngineAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.ProcessEngineRemove;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;

import java.util.Arrays;
import java.util.Collection;

public class ProcessEngineDefinition extends PersistentResourceDefinition {

  public static final ProcessEngineDefinition INSTANCE = new ProcessEngineDefinition();

  private ProcessEngineDefinition() {
    super(BpmPlatformExtension.PROCESS_ENGINES_PATH,
        BpmPlatformExtension.getResourceDescriptionResolver(ModelConstants.PROCESS_ENGINE),
        ProcessEngineAdd.INSTANCE,
        ProcessEngineRemove.INSTANCE);
  }

  @Override
  public Collection<AttributeDefinition> getAttributes() {
    return Arrays.asList(SubsystemAttributeDefinitons.PROCESS_ENGINE_ATTRIBUTES);
  }

}
