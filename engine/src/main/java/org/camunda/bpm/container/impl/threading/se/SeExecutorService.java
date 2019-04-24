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
package org.camunda.bpm.container.impl.threading.se;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;

/**
 * @author Daniel Meyer
 *
 */
public class SeExecutorService implements ExecutorService {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected ThreadPoolExecutor threadPoolExecutor;

  public SeExecutorService(ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
  }

  public boolean schedule(Runnable runnable, boolean isLongRunning) {

    if(isLongRunning) {
      return executeLongRunning(runnable);

    } else {
      return executeShortRunning(runnable);

    }
  }

  protected boolean executeLongRunning(Runnable runnable) {
    new Thread(runnable).start();
    return true;
  }

  protected boolean executeShortRunning(Runnable runnable) {

    try {
      threadPoolExecutor.execute(runnable);
      return true;
    }
    catch (RejectedExecutionException e) {
      LOG.debugRejectedExecutionException(e);
      return false;
    }

  }

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new ExecuteJobsRunnable(jobIds, processEngine);
  }

}
