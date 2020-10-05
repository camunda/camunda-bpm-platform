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
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.SingleConsumerCondition;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.FetchAndLockHandler;
import org.camunda.bpm.engine.rest.util.EngineUtil;


/**
 * @author Tassilo Weidner
 */
public class FetchAndLockHandlerImpl implements Runnable, FetchAndLockHandler {

  private static final Logger LOG = Logger.getLogger(FetchAndLockHandlerImpl.class.getName());

  protected static final String UNIQUE_WORKER_REQUEST_PARAM_NAME = "fetch-and-lock-unique-worker-request";

  protected static final long PENDING_REQUEST_FETCH_INTERVAL = 30L * 1000;
  protected static final long MAX_BACK_OFF_TIME = Long.MAX_VALUE;
  protected static final long MAX_REQUEST_TIMEOUT = 1800000; // 30 minutes

  protected SingleConsumerCondition condition;

  protected BlockingQueue<FetchAndLockRequest> queue = new ArrayBlockingQueue<>(200);
  protected List<FetchAndLockRequest> pendingRequests = new ArrayList<>();
  protected List<FetchAndLockRequest> newRequests = new ArrayList<>();

  protected Thread handlerThread = new Thread(this, this.getClass().getSimpleName());

  protected volatile boolean isRunning = false;

  protected boolean isUniqueWorkerRequest = false;

  public FetchAndLockHandlerImpl() {
    this.condition = new SingleConsumerCondition(handlerThread);
  }

  @Override
  public void run() {
    while (isRunning) {
      try {
        acquire();
      }
      catch (Exception e) {
        // what ever happens, don't leave the loop
      }
    }

    rejectPendingRequests();
  }

  protected void acquire() {
    LOG.log(Level.FINEST, "Acquire start");

    queue.drainTo(newRequests);

    if (!newRequests.isEmpty()) {
      if (isUniqueWorkerRequest) {
        removeDuplicates();
      }

      pendingRequests.addAll(newRequests);
      newRequests.clear();
    }

    LOG.log(Level.FINEST, "Number of pending requests {0}", pendingRequests.size());

    long backoffTime = MAX_BACK_OFF_TIME; //timestamp

    Iterator<FetchAndLockRequest> iterator = pendingRequests.iterator();
    while (iterator.hasNext()) {

      FetchAndLockRequest pendingRequest = iterator.next();

      LOG.log(Level.FINEST, "Fetching tasks for request {0}", pendingRequest);

      FetchAndLockResult result = tryFetchAndLock(pendingRequest);

      LOG.log(Level.FINEST, "Fetch and lock result: {0}", result);

      if (result.wasSuccessful()) {

        List<LockedExternalTaskDto> lockedTasks = result.getTasks();

        if (!lockedTasks.isEmpty() || isExpired(pendingRequest)) {
          AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
          asyncResponse.resume(lockedTasks);

          LOG.log(Level.FINEST, "resume and remove request with {0}", lockedTasks);

          iterator.remove();
        }
        else {
          final long msUntilTimeout = pendingRequest.getTimeoutTimestamp() - ClockUtil.getCurrentTime().getTime();
          backoffTime = Math.min(backoffTime, msUntilTimeout);
        }
      }
      else {
        AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
        Throwable processEngineException = result.getThrowable();
        asyncResponse.resume(processEngineException);

        LOG.log(Level.FINEST, "Resume and remove request with error", processEngineException);

        iterator.remove();
      }
    }

    final long waitTime = Math.max(0, backoffTime);

    if (pendingRequests.isEmpty()) {
      suspend(waitTime);
    }
    else {
      // if there are pending requests, try fetch periodically to ensure tasks created on other
      // cluster nodes and tasks with expired timeouts can be fetched in a timely manner
      suspend(Math.min(PENDING_REQUEST_FETCH_INTERVAL, waitTime));
    }
  }

  protected void removeDuplicates() {
    for (FetchAndLockRequest newRequest : newRequests) {
      // remove any request from pendingRequests with the same worker id
      Iterator<FetchAndLockRequest> iterator = pendingRequests.iterator();
      while (iterator.hasNext()) {
        FetchAndLockRequest pendingRequest = iterator.next();
        if (pendingRequest.getDto().getWorkerId().equals(newRequest.getDto().getWorkerId())) {
          AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
          asyncResponse.cancel();

          iterator.remove();
        }
      }

    }
  }

  @Override
  public void start() {
    if (isRunning) {
      return;
    }

    isRunning = true;
    handlerThread.start();

    ProcessEngineImpl.EXT_TASK_CONDITIONS.addConsumer(condition);
  }

  @Override
  public void shutdown() {
    try {
      ProcessEngineImpl.EXT_TASK_CONDITIONS.removeConsumer(condition);
    }
    finally {
      isRunning = false;
      condition.signal();
    }

    try {
      handlerThread.join();
    } catch (InterruptedException e) {
      LOG.log(Level.WARNING, "Shutting down the handler thread failed", e);
    }
  }

  protected void suspend(long millis) {
    if (millis <= 0) {
      return;
    }

    suspendAcquisition(millis);
  }

  protected void suspendAcquisition(long millis) {
    try {
      if (queue.isEmpty() && isRunning) {
        LOG.log(Level.FINEST, "Suspend acquisition for {0}ms", millis);
        condition.await(millis);
        LOG.log(Level.FINEST, "Acquisition woke up");
      }
    }
    finally {
      if (handlerThread.isInterrupted()) {
        Thread.currentThread().interrupt();
      }
    }
  }

  protected void addRequest(FetchAndLockRequest request) {
    if (!queue.offer(request)) {
      AsyncResponse asyncResponse = request.getAsyncResponse();
      errorTooManyRequests(asyncResponse);
    }

    condition.signal();
  }

  protected FetchAndLockResult tryFetchAndLock(FetchAndLockRequest request) {

    ProcessEngine processEngine = null;
    IdentityService identityService = null;
    FetchAndLockResult result = null;

    try {
      processEngine = getProcessEngine(request);

      identityService = processEngine.getIdentityService();
      identityService.setAuthentication(request.getAuthentication());

      FetchExternalTasksExtendedDto fetchingDto = request.getDto();
      List<LockedExternalTaskDto> lockedTasks = executeFetchAndLock(fetchingDto, processEngine);
      result = FetchAndLockResult.successful(lockedTasks);
    }
    catch (Exception e) {
      result = FetchAndLockResult.failed(e);
    }
    finally {
      if (identityService != null) {
        identityService.clearAuthentication();
      }
    }

    return result;
  }

  protected List<LockedExternalTaskDto> executeFetchAndLock(FetchExternalTasksExtendedDto fetchingDto, ProcessEngine processEngine) {
    ExternalTaskQueryBuilder fetchBuilder = fetchingDto.buildQuery(processEngine);
    List<LockedExternalTask> externalTasks = fetchBuilder.execute();
    return LockedExternalTaskDto.fromLockedExternalTasks(externalTasks);
  }

  protected void errorTooManyRequests(AsyncResponse asyncResponse) {
    String errorMessage = "At the moment the server has to handle too many requests at the same time. Please try again later.";
    asyncResponse.resume(new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, errorMessage));
  }

  protected void rejectPendingRequests() {
    for (FetchAndLockRequest pendingRequest : pendingRequests) {
      AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
      asyncResponse.resume(new RestException(Status.INTERNAL_SERVER_ERROR, "Request rejected due to shutdown of application server."));
    }
  }

  protected ProcessEngine getProcessEngine(FetchAndLockRequest request) {
    String processEngineName = request.getProcessEngineName();
    return EngineUtil.lookupProcessEngine(processEngineName);
  }

  protected boolean isExpired(FetchAndLockRequest request) {
    long currentTime = ClockUtil.getCurrentTime().getTime();
    long timeout = request.getTimeoutTimestamp();
    return timeout <= currentTime;
  }

  @Override
  public void addPendingRequest(FetchExternalTasksExtendedDto dto, AsyncResponse asyncResponse, ProcessEngine processEngine) {
    Long asyncResponseTimeout = dto.getAsyncResponseTimeout();
    if (asyncResponseTimeout != null && asyncResponseTimeout > MAX_REQUEST_TIMEOUT) {
      asyncResponse.resume(new InvalidRequestException(Status.BAD_REQUEST, "The asynchronous response timeout cannot be set to a value greater than "
          + MAX_REQUEST_TIMEOUT + " milliseconds"));
      return;
    }

    IdentityService identityService = processEngine.getIdentityService();
    Authentication authentication = identityService.getCurrentAuthentication();
    String processEngineName = processEngine.getName();

    FetchAndLockRequest incomingRequest = new FetchAndLockRequest()
      .setProcessEngineName(processEngineName)
      .setAsyncResponse(asyncResponse)
      .setAuthentication(authentication)
      .setDto(dto);

    LOG.log(Level.FINEST, "New request: {0}", incomingRequest);

    FetchAndLockResult result = tryFetchAndLock(incomingRequest);

    LOG.log(Level.FINEST, "Fetch and lock result: {0}", result);

    if (result.wasSuccessful()) {
      List<LockedExternalTaskDto> lockedTasks = result.getTasks();
      if (!lockedTasks.isEmpty() || dto.getAsyncResponseTimeout() == null) { // response immediately if tasks available
        asyncResponse.resume(lockedTasks);

        LOG.log(Level.FINEST, "Resuming request with {0}", lockedTasks);
      } else {
        addRequest(incomingRequest);

        LOG.log(Level.FINEST, "Deferred request");
      }
    }
    else {
      Throwable processEngineException = result.getThrowable();
      asyncResponse.resume(processEngineException);

      LOG.log(Level.FINEST, "Resuming request with error", processEngineException);
    }
  }

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = null;

    if (servletContextEvent != null) {
      servletContext = servletContextEvent.getServletContext();

      if (servletContext != null) {
        parseUniqueWorkerRequestParam(servletContext.getInitParameter(UNIQUE_WORKER_REQUEST_PARAM_NAME));
      }
    }
  }

  protected void parseUniqueWorkerRequestParam(String uniqueWorkerRequestParam) {
    if (uniqueWorkerRequestParam != null) {
      isUniqueWorkerRequest = Boolean.valueOf(uniqueWorkerRequestParam);
    } else {
      isUniqueWorkerRequest = false; // default configuration
    }
  }

  public List<FetchAndLockRequest> getPendingRequests() {
    return pendingRequests;
  }
}
