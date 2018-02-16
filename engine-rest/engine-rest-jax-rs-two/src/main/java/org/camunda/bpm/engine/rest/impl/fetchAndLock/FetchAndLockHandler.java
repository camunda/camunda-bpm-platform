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
package org.camunda.bpm.engine.rest.impl.fetchAndLock;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockHandler implements Runnable {

  private static final long MAX_BACK_OFF_TIME = Long.MAX_VALUE;
  private static final long MIN_BACK_OFF_TIME = 3000; // 3 seconds
  private static final long DEFAULT_BACK_OFF_TIME = 15000; // 15 seconds
  public static final long MIN_TIMEOUT = 60000; // 1 minute
  public static final long MAX_TIMEOUT = 1800000; // 30 minutes
  private static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

  private List<FetchAndLockRequest> pendingRequests = new CopyOnWriteArrayList<FetchAndLockRequest>();
  private Thread handlerThread = new Thread(this, this.getClass().getSimpleName());
  private boolean isRunning = true;

  public FetchAndLockHandler() {
    handlerThread.start();
  }

  @Override
  public void run() {
    while (isRunning) {
      long backOffTime;
      if (pendingRequests.isEmpty()) {
        backOffTime = MAX_BACK_OFF_TIME;
      } else {
        for (FetchAndLockRequest pendingRequest : pendingRequests) {
          List<LockedExternalTaskDto> lockedTasks = tryFetchAndLock(pendingRequest);
          if (lockedTasks != null) { // if false not authorized; pending request has already been removed
            FetchExternalTasksExtendedDto dto = pendingRequest.getDto();
            long asyncResponseTimeout = dto.getAsyncResponseTimeout();
            long currentTime = ClockUtil.getCurrentTime().getTime();
            long requestTime = pendingRequest.getRequestTime().getTime();
            if (!lockedTasks.isEmpty() || (requestTime + asyncResponseTimeout) <= currentTime) {
              pendingRequests.remove(pendingRequest);
              AsyncResponse asyncResponse = pendingRequest.getAsyncResponse();
              asyncResponse.resume(lockedTasks);
            }
          }
        }

        if (!pendingRequests.isEmpty()) {
          FetchAndLockRequest pendingRequest = pendingRequests.get(0); // get pending request with minimum slack time
          FetchExternalTasksExtendedDto dto = pendingRequest.getDto();
          long asyncResponseTimeout = dto.getAsyncResponseTimeout();
          long currentTime = ClockUtil.getCurrentTime().getTime();
          long requestTime = pendingRequest.getRequestTime().getTime();
          long slackTime = (requestTime + asyncResponseTimeout) - currentTime;
          if (slackTime > DEFAULT_BACK_OFF_TIME) {
            long dividedSlackTime = slackTime / 10;
            backOffTime = dividedSlackTime > DEFAULT_BACK_OFF_TIME ? dividedSlackTime : DEFAULT_BACK_OFF_TIME;
          } else {
            backOffTime = MIN_BACK_OFF_TIME;
          }
        } else {
          backOffTime = MAX_BACK_OFF_TIME;
        }
      }

      try {
        Thread.sleep(backOffTime);
      } catch (InterruptedException ignored) { }
    }
  }

  private AuthenticationResult enforceAuthentication(FetchAndLockRequest request) {
    AuthenticationResult authenticationResult =
      extractAuthenticatedUser(request.getProcessEngine(), request.getAuthHeader());
    if (!authenticationResult.isAuthenticated()) {
      AsyncResponse asyncResponse = request.getAsyncResponse();
      NotAuthorizedException notAuthorizedException = new NotAuthorizedException(BASIC_AUTH_HEADER_PREFIX +
        "realm=\"" + request.getProcessEngine().getName() + "\"");
      asyncResponse.resume(notAuthorizedException);
      pendingRequests.remove(request);
    }

    return authenticationResult;
  }

  private List<LockedExternalTaskDto> tryFetchAndLock(FetchAndLockRequest request) {
    AuthenticationResult authenticationResult = enforceAuthentication(request);
    if (authenticationResult.isAuthenticated()) {
      List<LockedExternalTaskDto> lockedTasks = Collections.emptyList();

      try {
        setAuthenticatedUser(request.getProcessEngine(), authenticationResult.getAuthenticatedUser());
        lockedTasks = delegateFetchAndLock(request);
        clearAuthentication(request.getProcessEngine());
      } catch (ProcessEngineException e) {
        processEngineException(request, e);
      }

      return lockedTasks;
    }

    return null; // not authorized
  }

  private List<LockedExternalTaskDto> delegateFetchAndLock(FetchAndLockRequest request) {
    FetchExternalTasksExtendedDto fetchingDto = request.getDto();
    ExternalTaskQueryBuilder fetchBuilder = request.getProcessEngine()
      .getExternalTaskService()
      .fetchAndLock(fetchingDto.getMaxTasks(), fetchingDto.getWorkerId(), fetchingDto.isUsePriority());

    if (fetchingDto.getTopics() != null) {
      for (FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto topicDto : fetchingDto.getTopics()) {
        ExternalTaskQueryTopicBuilder topicFetchBuilder =
          fetchBuilder.topic(topicDto.getTopicName(), topicDto.getLockDuration());

        if (topicDto.getVariables() != null) {
          topicFetchBuilder = topicFetchBuilder.variables(topicDto.getVariables());
        }

        if (topicDto.isDeserializeValues()) {
          topicFetchBuilder = topicFetchBuilder.enableCustomObjectDeserialization();
        }

        fetchBuilder = topicFetchBuilder;
      }
    }

    List<LockedExternalTask> tasks = fetchBuilder.execute();
    return LockedExternalTaskDto.fromLockedExternalTasks(tasks);
  }

  private void invalidRequest(AsyncResponse asyncResponse, String message) {
    InvalidRequestException invalidRequestException = new InvalidRequestException(Status.BAD_REQUEST, message);
    asyncResponse.resume(invalidRequestException);
  }

  private void processEngineException(FetchAndLockRequest request, ProcessEngineException exception) {
    pendingRequests.remove(request);
    AsyncResponse asyncResponse = request.getAsyncResponse();
    asyncResponse.resume(new InvalidRequestException(Status.BAD_REQUEST, exception, exception.getMessage()));
  }

  public void addPendingRequest(FetchExternalTasksExtendedDto dto,
                         AsyncResponse asyncResponse, HttpHeaders headers, ProcessEngine processEngine) {
    if (dto == null) {
      invalidRequest(asyncResponse, "The body of the request cannot be empty");
      return;
    }

    Long asyncResponseTimeout = dto.getAsyncResponseTimeout();
    if (asyncResponseTimeout != null && asyncResponseTimeout < MIN_TIMEOUT) {
      invalidRequest(asyncResponse, "The asynchronous response timeout cannot be set to a value less than "
        + MIN_TIMEOUT + " milliseconds");
      return;
    }

    if (asyncResponseTimeout != null && asyncResponseTimeout > MAX_TIMEOUT) {
      invalidRequest(asyncResponse, "The asynchronous response timeout cannot be set to a value greater than "
        + MAX_TIMEOUT + " milliseconds");
      return;
    }

    FetchAndLockRequest incomingRequest = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse)
      .setDto(dto);

    String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && !authHeader.isEmpty()) {
      incomingRequest.setAuthHeader(authHeader);
    }

    List<LockedExternalTaskDto> lockedTasks = tryFetchAndLock(incomingRequest);
    if (lockedTasks != null) {
      if (dto.getAsyncResponseTimeout() == null || !lockedTasks.isEmpty()) { // response immediately if tasks available
        asyncResponse.resume(lockedTasks);
      } else {
        pendingRequests.add(incomingRequest);

        if (pendingRequests.size() > 1) {
          ArrayList<FetchAndLockRequest> temp = new ArrayList<FetchAndLockRequest>();
          temp.addAll(pendingRequests);
          Collections.sort(temp); // sort according to slack time by ascending order
          pendingRequests.clear();
          pendingRequests.addAll(temp);
        }

        handlerThread.interrupt();
      }
    }
  }

  private void setAuthenticatedUser(ProcessEngine engine, String userId) {
    List<String> groupIds = getGroupsOfUser(engine, userId);
    List<String> tenantIds = getTenantsOfUser(engine, userId);

    engine.getIdentityService().setAuthentication(userId, groupIds, tenantIds);
  }

  private List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
    List<Group> groups = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  private List<String> getTenantsOfUser(ProcessEngine engine, String userId) {
    List<Tenant> tenants = engine.getIdentityService().createTenantQuery()
      .userMember(userId)
      .includingGroupsOfUser(true)
      .list();

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant : tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  private void clearAuthentication(ProcessEngine engine) {
    engine.getIdentityService().clearAuthentication();
  }

  private AuthenticationResult extractAuthenticatedUser(ProcessEngine engine, String authHeader) {
    if (authHeader != null && authHeader.startsWith(BASIC_AUTH_HEADER_PREFIX)) {
      String encodedCredentials = authHeader.substring(BASIC_AUTH_HEADER_PREFIX.length());
      String decodedCredentials = new String(Base64.decodeBase64(encodedCredentials));
      int firstColonIndex = decodedCredentials.indexOf(":");

      if (firstColonIndex == -1) {
        return AuthenticationResult.unsuccessful();
      } else {
        String userName = decodedCredentials.substring(0, firstColonIndex);
        String password = decodedCredentials.substring(firstColonIndex + 1);
        if (isAuthenticated(engine, userName, password)) {
          return AuthenticationResult.successful(userName);
        } else {
          return AuthenticationResult.unsuccessful(userName);
        }
      }
    } else {
      return AuthenticationResult.unsuccessful();
    }
  }

  private boolean isAuthenticated(ProcessEngine engine, String userName, String password) {
    return engine.getIdentityService().checkPassword(userName, password);
  }

  public Thread getHandlerThread() {
    return handlerThread;
  }

  public void stop() {
    isRunning = false;
  }

  public List<FetchAndLockRequest> getPendingRequests() {
    return pendingRequests;
  }

}
