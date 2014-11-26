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
package org.camunda.bpm.container.impl.deployment.jobexecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedThreadPool;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

/**
 * <p>Deployment operation step responsible for deploying a thread pool for the JobExecutor</p>
 *
 * @author Daniel Meyer
 *
 */
public class StartManagedThreadPoolStep extends DeploymentOperationStep {

  public String getName() {
    return "Deploy Job Executor Thread Pool";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    // TODO: read these values from JobExecutorXml properties
    int queueSize = 3;
    int corePoolSize = 3;
    int maxPoolSize = 10;

    // initialize Queue & Executor services
    BlockingQueue<Runnable> threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
    threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

    // construct the service for the thread pool
    JmxManagedThreadPool managedThreadPool = new JmxManagedThreadPool(threadPoolQueue, threadPoolExecutor);

    // install the service into the container
    serviceContainer.startService(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_EXECUTOR, managedThreadPool);

  }

}
