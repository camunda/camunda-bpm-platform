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
package org.camunda.bpm.container.impl.jboss.extension.resource;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionAdd;
import org.camunda.bpm.container.impl.jboss.extension.handler.JobAcquisitionRemove;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

import java.util.Arrays;
import java.util.Collection;

public class JobAcquisitionDefinition extends PersistentResourceDefinition {

  public static final JobAcquisitionDefinition INSTANCE = new JobAcquisitionDefinition();

  private JobAcquisitionDefinition() {
    super(BpmPlatformExtension.JOB_ACQUISTIONS_PATH,
        BpmPlatformExtension.getResourceDescriptionResolver(ModelConstants.JOB_ACQUISITION),
        JobAcquisitionAdd.INSTANCE,
        JobAcquisitionRemove.INSTANCE);
  }

  @Override
  public Collection<AttributeDefinition> getAttributes() {
    return Arrays.asList(SubsystemAttributeDefinitons.JOB_ACQUISITION_ATTRIBUTES);
  }

  @Override
  public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
    super.registerAttributes(resourceRegistration);
  }
}
