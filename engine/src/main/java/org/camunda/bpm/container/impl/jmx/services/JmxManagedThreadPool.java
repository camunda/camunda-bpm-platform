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
package org.camunda.bpm.container.impl.jmx.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.threading.se.SeExecutorService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class JmxManagedThreadPool extends SeExecutorService implements JmxManagedThreadPoolMBean, PlatformService<JmxManagedThreadPool> {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected final BlockingQueue<Runnable> threadPoolQueue;

  public JmxManagedThreadPool(BlockingQueue<Runnable> queue, ThreadPoolExecutor executor) {
    super(executor);
    threadPoolQueue = queue;
  }

  public void start(PlatformServiceContainer mBeanServiceContainer) {
    // nothing to do
  }

  public void stop(PlatformServiceContainer mBeanServiceContainer) {

    // clear the queue
    threadPoolQueue.clear();

    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
        LOG.timeoutDuringShutdownOfThreadPool(60, TimeUnit.SECONDS);
      }
    }
    catch (InterruptedException e) {
      LOG.interruptedWhileShuttingDownThreadPool(e);
    }

  }

  public JmxManagedThreadPool getValue() {
    return this;
  }

  public void setCorePoolSize(int corePoolSize) {
    threadPoolExecutor.setCorePoolSize(corePoolSize);
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);
  }

  public int getMaximumPoolSize() {
    return threadPoolExecutor.getMaximumPoolSize();
  }

  public void setKeepAliveTime(long time, TimeUnit unit) {
    threadPoolExecutor.setKeepAliveTime(time, unit);
  }

  public void purgeThreadPool() {
    threadPoolExecutor.purge();
  }

  public int getPoolSize() {
    return threadPoolExecutor.getPoolSize();
  }

  public int getActiveCount() {
    return threadPoolExecutor.getActiveCount();
  }

  public int getLargestPoolSize() {
    return threadPoolExecutor.getLargestPoolSize();
  }

  public long getTaskCount() {
    return threadPoolExecutor.getTaskCount();
  }

  public long getCompletedTaskCount() {
    return threadPoolExecutor.getCompletedTaskCount();
  }

  public int getQueueCount() {
    return threadPoolQueue.size();
  }

  public ThreadPoolExecutor getThreadPoolExecutor() {
    return threadPoolExecutor;
  }
}
