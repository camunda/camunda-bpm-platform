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
package org.camunda.bpm.container.impl.ejb.deployment;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

/**
 * <p>Deployment operation responsible for stopping a service which represents a Proxy to the
 * JCA-Backed {@link ExecutorService}</p>
 *
 * @author Daniel Meyer
 *
 */
public class StopJcaExecutorServiceStep extends DeploymentOperationStep {

  public String getName() {
    return "Stop JCA Executor Service";
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    serviceContainer.stopService(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_EXECUTOR);

  }

}
