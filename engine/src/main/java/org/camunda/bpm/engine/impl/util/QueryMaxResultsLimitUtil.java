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
package org.camunda.bpm.engine.impl.util;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;

public class QueryMaxResultsLimitUtil {

  public static void checkMaxResultsLimit(int resultsCount, int maxResultsLimit,
                                          boolean isUserAuthenticated) {
    if (isUserAuthenticated && maxResultsLimit < Integer.MAX_VALUE) {
      if (resultsCount == Integer.MAX_VALUE) {
        throw new BadUserRequestException("An unbound number of results is forbidden!");

      } else if (resultsCount > maxResultsLimit) {
        throw new BadUserRequestException("Max results limit of " + maxResultsLimit + " exceeded!");

      }
    }
  }

  public static void checkMaxResultsLimit(int resultsCount,
                                          ProcessEngineConfigurationImpl processEngineConfig) {
    // method is used in webapps
    int maxResultsLimit = processEngineConfig.getQueryMaxResultsLimit();
    checkMaxResultsLimit(resultsCount, maxResultsLimit, isUserAuthenticated(processEngineConfig));
  }

  public static void checkMaxResultsLimit(int resultsCount) {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        Context.getProcessEngineConfiguration();
    if (processEngineConfiguration == null) {
      throw new ProcessEngineException("Command context unset.");
    }

    checkMaxResultsLimit(resultsCount, getMaxResultsLimit(processEngineConfiguration),
        isUserAuthenticated(processEngineConfiguration));
  }

  protected static boolean isUserAuthenticated(ProcessEngineConfigurationImpl processEngineConfig) {
    String userId = getAuthenticatedUserId(processEngineConfig);
    return userId != null && !userId.isEmpty();
  }

  protected static String getAuthenticatedUserId(
      ProcessEngineConfigurationImpl processEngineConfig) {
    IdentityService identityService = processEngineConfig.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    if(currentAuthentication == null) {
      return null;
    } else {
      return currentAuthentication.getUserId();
    }
  }

  protected static int getMaxResultsLimit(ProcessEngineConfigurationImpl processEngineConfig) {
    return processEngineConfig.getQueryMaxResultsLimit();
  }

}
