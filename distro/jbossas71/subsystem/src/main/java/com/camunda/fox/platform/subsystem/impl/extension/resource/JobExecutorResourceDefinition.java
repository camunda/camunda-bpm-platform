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

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;
import com.camunda.fox.platform.subsystem.impl.extension.ModelConstants;
import com.camunda.fox.platform.subsystem.impl.extension.handler.JobExecutorAdd;


public class JobExecutorResourceDefinition extends SimpleResourceDefinition {

  private static final PathElement JOB_EXECUTOR_PATH  = PathElement.pathElement(ModelConstants.JOB_EXECUTOR);
  
  public JobExecutorResourceDefinition() {
    super(JOB_EXECUTOR_PATH, FoxPlatformExtension.getResourceDescriptionResolver(ModelConstants.JOB_EXECUTOR),
            JobExecutorAdd.INSTANCE, null);
  }
  
}
