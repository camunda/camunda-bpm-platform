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

import org.activiti.engine.ProcessEngine;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessEngineDependencyAttachements {

  private static AttachmentKey<ProcessEngine> defaultEngine = AttachmentKey.create(ProcessEngine.class);
  private static AttachmentKey<AttachmentList<ProcessEngine>> engines = AttachmentKey.createList(ProcessEngine.class);
  
  public static AttachmentKey<AttachmentList<ProcessEngine>> getDependentEnginesKey() {
    return engines;
  }
  
  public static AttachmentKey<ProcessEngine> getDefaultEngineKey() {
    return defaultEngine;
  }
  
}
