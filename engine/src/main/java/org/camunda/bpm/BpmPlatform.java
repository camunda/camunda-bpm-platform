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
package org.camunda.bpm;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;


/**
 * <p>Provides access to the camunda BPM platform services.</p>
 * 
 * @author Daniel Meyer
 *
 */
public final class BpmPlatform {
  
  public final static String JNDI_NAME_PREFIX = "java:global";
  public final static String APP_JNDI_NAME = "camunda-bpm-platform";
  public final static String MODULE_JNDI_NAME = "process-engine";
  
  public final static String PROCESS_ENGINE_SERVICE_NAME = "ProcessEngineService!org.camunda.bpm.ProcessEngineService";
  public final static String PROCESS_APPLICATION_SERVICE_NAME = "ProcessApplicationService!org.camunda.bpm.ProcessApplicationService";
  
  public final static String PROCESS_ENGINE_SERVICE_JNDI_NAME = JNDI_NAME_PREFIX + "/" + APP_JNDI_NAME + "/" + MODULE_JNDI_NAME + "/" + PROCESS_ENGINE_SERVICE_NAME;
  public final static String PROCESS_APPLICATION_SERVICE_JNDI_NAME = JNDI_NAME_PREFIX + "/" + APP_JNDI_NAME + "/" + MODULE_JNDI_NAME + "/" + PROCESS_APPLICATION_SERVICE_NAME;
  
  public static ProcessEngineService getProcessEngineService() {
    return RuntimeContainerDelegate.INSTANCE.get().getProcessEngineService();
  }
  
  public static ProcessApplicationService getProcessApplicationService() {
    return RuntimeContainerDelegate.INSTANCE.get().getProcessApplicationService();
  }
  
  public static ProcessEngine getDefaultProcessEngine() {
    return getProcessEngineService().getDefaultProcessEngine();
  }
  
}