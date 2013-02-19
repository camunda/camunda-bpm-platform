/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.subsystem.impl.deployment.marker;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

import com.camunda.fox.platform.subsystem.impl.service.ContainerProcessEngineController;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessEngineDependencyAttachements {

  private static AttachmentKey<ContainerProcessEngineController> defaultEngine = AttachmentKey.create(ContainerProcessEngineController.class);
  private static AttachmentKey<AttachmentList<ContainerProcessEngineController>> engines = AttachmentKey.createList(ContainerProcessEngineController.class);
  
  public static AttachmentKey<AttachmentList<ContainerProcessEngineController>> getDependentEnginesKey() {
    return engines;
  }
  
  public static AttachmentKey<ContainerProcessEngineController> getDefaultEngineKey() {
    return defaultEngine;
  }
  
}
