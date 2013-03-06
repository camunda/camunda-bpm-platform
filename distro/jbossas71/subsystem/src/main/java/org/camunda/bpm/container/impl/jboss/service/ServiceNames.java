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
package org.camunda.bpm.container.impl.jboss.service;

import org.jboss.msc.service.ServiceName;

/**
 * <p>All ServiceName references run through here.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ServiceNames {
  
  private final static ServiceName BPM_PLATFORM = ServiceName.of("org", "camunda", "bpm", "platform");
  
  private final static ServiceName PROCESS_ENGINE = BPM_PLATFORM.append("process-engine");
  private final static ServiceName DEFAULT_PROCESS_ENGINE = PROCESS_ENGINE.append("default");
  
  private final static ServiceName MSC_RUNTIME_CONTAINER_DELEGATE = BPM_PLATFORM.append("runtime-container");
  
  private final static ServiceName PROCESS_APPLICATION = BPM_PLATFORM.append("process-application");  
  
  /**
   * Returns the service name for a {@link MscManagedProcessEngine}. 
   * 
   * @param the
   *          name of the process engine
   * @return the composed service name
   */
  public static ServiceName forManagedProcessEngine(String processEngineName) {
    return PROCESS_ENGINE.append(processEngineName);
  }
  
  /**
   * @return the {@link ServiceName} for the default
   *         {@link MscManagedProcessEngine}. This is a constant name since
   *         there can only be one default process engine.
   */
  public static ServiceName forDefaultProcessEngine() {
    return DEFAULT_PROCESS_ENGINE;
  }
  
  /**
   * @return the {@link ServiceName} for the {@link MscRuntimeContainerDelegate}
   */
  public static ServiceName forMscRuntimeContainerDelegate() {
    return MSC_RUNTIME_CONTAINER_DELEGATE;
  }
  
  /**
   * <p>Returns the name for a {@link ProcessApplicationDeploymentService} given
   * the name of the deployment unit and the name of the deployment.</p>
   * 
   * @param processApplicationName
   * @param deploymentId
   */
  public static ServiceName forProcessApplicationDeploymentService(String deploymentUnitName, String deploymentName) {
    return PROCESS_APPLICATION.append("deployment").append(deploymentUnitName).append(deploymentName);
  }

  /**
   * @return the {@link ServiceName} that is the longest common prefix of all 
   * ServiceNames used for {@link MscManagedProcessEngine}.
   */
  public static ServiceName forManagedProcessEngines() {
    return PROCESS_ENGINE;
  }
  
  /**
   * @return the {@link ServiceName} that is the longest common prefix of all 
   * ServiceNames used for {@link MscManagedProcessApplication}.
   */
  public static ServiceName forManagedProcessApplications() {
    return PROCESS_APPLICATION.append("runtime");
  }
  
  /**
   * @param applicationName
   * @return the name to be used for an {@link MscManagedProcessApplication} service.
   */
  public static ServiceName forManagedProcessApplication(String applicationName) {
    return PROCESS_APPLICATION.append("runtime").append(applicationName);
  }
  
  /**
   * @param applicationName
   * @return the name to be used for an {@link MscManagedProcessApplication} service.
   */
  public static ServiceName forProcessApplicationStartService(String deploymentUnitName) {
    return PROCESS_APPLICATION.append("start").append(deploymentUnitName);
  }
  
  
}
