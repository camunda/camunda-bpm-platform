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
package org.camunda.bpm.container.impl.jboss.service;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;


public class MscExecutorService implements Service<MscExecutorService>, ExecutorService {

  private static Logger log = Logger.getLogger(MscExecutorService.class.getName());

  protected final Supplier<ManagedQueueExecutorService> managedQueueSupplier;
  protected final Consumer<ExecutorService> provider;

  private long lastWarningLogged = System.currentTimeMillis();

  public MscExecutorService(Supplier<ManagedQueueExecutorService> managedQueueSupplier, Consumer<ExecutorService> provider) {
    this.managedQueueSupplier = managedQueueSupplier;
    this.provider = provider;
  }

  @Override
  public MscExecutorService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  @Override
  public void start(StartContext context) throws StartException {
    provider.accept(this);
  }

  @Override
  public void stop(StopContext context) {
    provider.accept(null);
  }

  @Override
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new ExecuteJobsRunnable(jobIds, processEngine);
  }

  @Override
  public boolean schedule(Runnable runnable, boolean isLongRunning) {

    if(isLongRunning) {
      return scheduleLongRunningWork(runnable);

    } else {
      return scheduleShortRunningWork(runnable);

    }

  }

  protected boolean scheduleShortRunningWork(Runnable runnable) {

    ManagedQueueExecutorService managedQueueExecutorService = managedQueueSupplier.get();

    try {

      managedQueueExecutorService.executeBlocking(runnable);
      return true;

    } catch (InterruptedException e) {
      // the the acquisition thread is interrupted, this probably means the app server is turning the lights off -> ignore
    } catch (Exception e) {
      // we must be able to schedule this
      log.log(Level.WARNING,  "Cannot schedule long running work.", e);
    }

    return false;
  }

  protected boolean scheduleLongRunningWork(Runnable runnable) {

    final ManagedQueueExecutorService managedQueueExecutorService = managedQueueSupplier.get();

    boolean rejected = false;
    try {

      // wait for 2 seconds for the job to be accepted by the pool.
      managedQueueExecutorService.executeBlocking(runnable, 2, TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      // the acquisition thread is interrupted, this probably means the app server is turning the lights off -> ignore
    } catch (RejectedExecutionException e) {
      rejected = true;
    } catch (Exception e) {
      // if it fails for some other reason, log a warning message
      long now = System.currentTimeMillis();
      // only log every 60 seconds to prevent log flooding
      if((now-lastWarningLogged) >= (60*1000)) {
        log.log(Level.WARNING, "Unexpected Exception while submitting job to executor pool.", e);
      } else {
        log.log(Level.FINE, "Unexpected Exception while submitting job to executor pool.", e);
      }
    }

    return !rejected;

  }

}
