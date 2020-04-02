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
package org.camunda.bpm.spring.boot.starter.util;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.User;
import org.camunda.commons.logging.BaseLogger;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.Set;

public class SpringBootProcessEngineLogger extends BaseLogger {
  public static final String PROJECT_CODE = "STARTER";
  public static final String PROJECT_ID = "SB";
  public static final String PACKAGE = "org.camunda.bpm.spring.boot";

  public static final SpringBootProcessEngineLogger LOG = createLogger(SpringBootProcessEngineLogger.class, PROJECT_CODE, PACKAGE, PROJECT_ID);

  public void creatingInitialAdminUser(User adminUser) {
    logInfo("010", "creating initial Admin User: {}", adminUser);
  }

  public void skipAdminUserCreation(User existingUser) {
    logInfo("011", "skip creating initial Admin User, user does exist: {}", existingUser);
  }

  public void createInitialFilter(Filter filter) {
    logInfo("015", "create initial filter: id={} name={}", filter.getId(), filter.getName());
  }

  public void skipCreateInitialFilter(String filterName) {
    logInfo("016",
        "Skip initial filter creation, the filter with this name already exists: {}",
        filterName);
  }

  public void skipAutoDeployment() {
    logInfo("020", "ProcessApplication enabled: autoDeployment via springConfiguration#deploymentResourcePattern is disabled");
  }

  public void autoDeployResources(Set<Resource> resources) {
    logInfo("021", "Auto-Deploying resources: {}", resources);
  }

  public void enterLicenseKey(URL licenseKeyFile) {
    logInfo("030", "Setting up license key: {}", licenseKeyFile);
  }

  public void enterLicenseKeyFailed(URL licenseKeyFile, Exception e) {
    logWarn("031", "Failed setting up license key: {}", licenseKeyFile, e);
  }

  public void configureJobExecutorPool(Integer corePoolSize, Integer maxPoolSize) {
    logInfo("040", "Setting up jobExecutor with corePoolSize={}, maxPoolSize:{}", corePoolSize, maxPoolSize);
  }

  public SpringBootStarterException exceptionDuringBinding(String message) {
    return new SpringBootStarterException(exceptionMessage(
        "050", message));
  }

}