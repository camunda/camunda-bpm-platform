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
package com.camunda.fox.platform.subsystem.impl.service;

import org.jboss.msc.service.ServiceName;

/**
 * <p>Class with service names</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ServiceNames {
  
  public final static ServiceName PLATFORM_SERVICE_ROOT = ServiceName.of("org", "camunda", "bpm", "platform");
  
  public final static ServiceName MANAGED_PROCESS_ENGINE = PLATFORM_SERVICE_ROOT.append("process-engine");
  public final static ServiceName DEFAULT_PROCESS_ENGINE = MANAGED_PROCESS_ENGINE.append("default");
  
  public final static ServiceName MSC_RUNTIME_CONTAINER_DELEGATE = PLATFORM_SERVICE_ROOT.append("container");
  
  public static ServiceName getManagedProcessEngineName(String processEngineName) {
    return MANAGED_PROCESS_ENGINE.append(processEngineName);
  }
  
  public static ServiceName getManagedProcessEngineStartName(String processEngineName) {
    return MANAGED_PROCESS_ENGINE.append("start-"+processEngineName);
  }
  
}
