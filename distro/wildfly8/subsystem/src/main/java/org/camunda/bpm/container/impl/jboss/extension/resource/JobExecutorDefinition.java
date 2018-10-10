package org.camunda.bpm.container.impl.jboss.extension.resource;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobExecutorAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobExecutorRemove;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;

import java.util.*;

public class JobExecutorDefinition extends PersistentResourceDefinition {

  public static final JobExecutorDefinition INSTANCE = new JobExecutorDefinition();

  private JobExecutorDefinition() {
    super(BpmPlatformExtension.JOB_EXECUTOR_PATH,
        BpmPlatformExtension.getResourceDescriptionResolver(ModelConstants.JOB_EXECUTOR),
        JobExecutorAdd.INSTANCE,
        JobExecutorRemove.INSTANCE);
  }

  @Override
  public Collection<AttributeDefinition> getAttributes() {
    return Arrays.asList(SubsystemAttributeDefinitons.JOB_EXECUTOR_ATTRIBUTES);
  }

  @Override
  protected List<? extends PersistentResourceDefinition> getChildren() {
    return Collections.singletonList(JobAcquisitionDefinition.INSTANCE);
  }

}
