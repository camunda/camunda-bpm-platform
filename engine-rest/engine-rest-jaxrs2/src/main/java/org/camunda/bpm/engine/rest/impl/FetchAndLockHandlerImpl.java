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
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.spi.FetchAndLockHandler;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockHandlerImpl implements Runnable, FetchAndLockHandler {

  protected static final long MAX_BACK_OFF_TIME = Long.MAX_VALUE;
  protected static final long MAX_TIMEOUT = 1800000; // 30 minutes

  protected BlockingQueue<FetchAndLockRequest> queue = new ArrayBlockingQueue<FetchAndLockRequest>(100);
  protected List<FetchAndLockRequest> pendingRequests = new ArrayList<FetchAndLockRequest>();

  protected final Object MONITOR = new Object();
  protected Thread handlerThread = new Thread(this, this.getClass().getSimpleName());

  protected boolean isWaiting = false;
  protected boolean isRunning = false;

  @Override
  public void run() {
    while (isRunning) {
      acquire();
    }

    rejectPendingRequests();
  }

  protected void acquire() {
    queue.drainTo(pendingRequests);

    long backoffTime = MAX_BACK_OFF_TIME;     //timestamp

    Iterator<FetchAndLockRequest> iterator = pendingRequests.iterator();
    while (iterator.hasNext()) {

      FetchAndLockRequest pendingRequest = iterator.next();

      long currentTime = ClockUtil.getCurrentTime().getTime();
      FetchAndLockResult result = tryFetchAndLock(pendingRequest);

      if (result.wasSuccessful()) {

        List<LockedExternalTaskDto> lockedTasks = result.getTasks();

        long timeout = pendingRequest.getTimeoutTimestamp();

        if (!lockedTasks.isEmpty() || timeout <= currentTime) {
          AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
          iterator.remove();
          asyncResponse.resume(lockedTasks);
        } else {
          if (timeout < backoffTime) {
            backoffTime = timeout;
          }
        }
      } else {
        AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
        iterator.remove();
        ProcessEngineException processEngineException = result.getProcessEngineException();
        asyncResponse.resume(processEngineException);
      }
    }

    suspend(Math.max(0, backoffTime - ClockUtil.getCurrentTime().getTime()));
  }

  @Override
  public void start() {
    if (isRunning) {
      return;
    }

    isRunning = true;
    handlerThread.start();
  }

  @Override
  public void shutdown() {
    synchronized (MONITOR) {
      isRunning = false;
      if (isWaiting) {
        MONITOR.notifyAll();
      }
    }
  }

  protected void suspend(long millis) {
    if (millis <= 0) {
      return;
    }

    if (!queue.isEmpty()) {
      return;
    }

    suspendAcquisition(millis);
  }

  protected void suspendAcquisition(long millis) {
    try {
      synchronized (MONITOR) {
        if (queue.isEmpty()) {
          isWaiting = true;
          MONITOR.wait(millis);
        }
      }

    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    finally {
      isWaiting = false;
    }
  }

  protected void addRequest(FetchAndLockRequest request) {
    if (!queue.offer(request)) {
      AsyncResponse asyncResponse = request.getAsyncResponse();
      errorTooManyRequests(asyncResponse);
    }

    notifyAcquisition();
  }

  protected void notifyAcquisition() {
    synchronized (MONITOR) {
      if (isWaiting) {
        MONITOR.notifyAll();
      }
    }
  }

  protected FetchAndLockResult tryFetchAndLock(FetchAndLockRequest request) {
    ProcessEngine processEngine = request.getProcessEngine();
    IdentityService identityService = processEngine.getIdentityService();

    FetchAndLockResult result;

    try {
      identityService.setAuthentication(request.getAuthentication());
      FetchExternalTasksExtendedDto fetchingDto = request.getDto();
      List<LockedExternalTaskDto> lockedTasks = executeFetchAndLock(fetchingDto, processEngine);
      result = FetchAndLockResult.successful(lockedTasks);
    }
    catch (ProcessEngineException e) {
      result = FetchAndLockResult.failed(e);
    }
    finally {
      identityService.clearAuthentication();
    }

    return result;
  }

  protected List<LockedExternalTaskDto> executeFetchAndLock(FetchExternalTasksExtendedDto fetchingDto, ProcessEngine processEngine) {
    ExternalTaskQueryBuilder fetchBuilder = fetchingDto.buildQuery(processEngine);
    List<LockedExternalTask> externalTasks = fetchBuilder.execute();
    return LockedExternalTaskDto.fromLockedExternalTasks(externalTasks);
  }

  protected void invalidRequest(AsyncResponse asyncResponse, String message) {
    InvalidRequestException invalidRequestException = new InvalidRequestException(Status.BAD_REQUEST, message);
    asyncResponse.resume(invalidRequestException);
  }

  protected void errorTooManyRequests(AsyncResponse asyncResponse) {
    String errorMessage = "At the moment the server has to handle too many requests at the same time. Please try again later.";
    InvalidRequestException invalidRequestException = new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, errorMessage); // status code 429 would fit better
    asyncResponse.resume(invalidRequestException);
  }

  protected void rejectPendingRequests() {
    for (FetchAndLockRequest pendingRequest : pendingRequests) {
      AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
      invalidRequest(asyncResponse, "Request rejected due to shutdown of application server.");
    }
  }

  @Override
  public void addPendingRequest(FetchExternalTasksExtendedDto dto, AsyncResponse asyncResponse, ProcessEngine processEngine) {
    Long asyncResponseTimeout = dto.getAsyncResponseTimeout();
    if (asyncResponseTimeout != null && asyncResponseTimeout > MAX_TIMEOUT) {
      invalidRequest(asyncResponse, "The asynchronous response timeout cannot be set to a value greater than "
        + MAX_TIMEOUT + " milliseconds");
      return;
    }

    IdentityService identityService = processEngine.getIdentityService();
    Authentication authentication = identityService.getCurrentAuthentication();

    FetchAndLockRequest incomingRequest = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse)
      .setAuthentication(authentication)
      .setDto(dto);

    FetchAndLockResult result = tryFetchAndLock(incomingRequest);

    if (result.wasSuccessful()) {
      List<LockedExternalTaskDto> lockedTasks = result.getTasks();
      if (!lockedTasks.isEmpty() || dto.getAsyncResponseTimeout() == null) { // response immediately if tasks available
        asyncResponse.resume(lockedTasks);
      } else {
        addRequest(incomingRequest);
      }
    }
    else {
      ProcessEngineException processEngineException = result.getProcessEngineException();
      asyncResponse.resume(processEngineException);
    }
  }

  public List<FetchAndLockRequest> getPendingRequests() {
    return pendingRequests;
  }

}
