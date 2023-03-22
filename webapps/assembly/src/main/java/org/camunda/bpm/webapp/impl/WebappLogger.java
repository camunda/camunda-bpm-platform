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
package org.camunda.bpm.webapp.impl;

import java.util.Date;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.commons.logging.BaseLogger;

public class WebappLogger extends BaseLogger {

  public static final String PROJECT_CODE = "WEBAPP";

  public static final WebappLogger INSTANCE = BaseLogger.createLogger(WebappLogger.class, PROJECT_CODE,
      "org.camunda.bpm.webapp", "00");

  public InvalidRequestException invalidRequestEngineNotFoundForName(String engineName) {
    return new InvalidRequestException(Status.BAD_REQUEST,
        "Process engine with name " + engineName + " does not exist");
  }

  public InvalidRequestException setupActionNotAvailable() {
    return new InvalidRequestException(Status.FORBIDDEN, "Setup action not available");
  }

  public RestException processEngineProviderNotFound() {
    return new RestException(Status.BAD_REQUEST, "Could not find an implementation of the " + ProcessEngineProvider.class + "- SPI");
  }

  public void infoWebappSuccessfulLogin(String username) {
    logInfo("001", "Successful login for user {}.", username);
  }

  public void infoWebappFailedLogin(String username, String reason) {
    logInfo("002", "Failed login attempt for user {}. Reason: {}", username, reason);
  }

  public void infoWebappLogout(String username) {
    logInfo("003", "Successful logout for user {}.", username);
  }

  public void traceCacheValidationTime(Date cacheValidationTime) {
    logTrace("004", "Cache validation time: {}", cacheValidationTime);
  }

  public void traceCacheValidationTimeUpdated(Date cacheValidationTime, Date newCacheValidationTime) {
    logTrace("005", "Cache validation time updated from: {} to: {}", cacheValidationTime, newCacheValidationTime);
  }

  public void traceAuthenticationUpdated(String engineName) {
    logTrace("006", "Authentication updated: {}", engineName);
  }

  public void traceAuthenticationRemoved(String engineName) {
    logTrace("007", "Authentication removed: {}", engineName);
  }


}
