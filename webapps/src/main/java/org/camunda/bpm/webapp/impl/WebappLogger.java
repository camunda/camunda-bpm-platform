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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.commons.logging.BaseLogger;

import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.INCLUDE_SUBDOMAINS_DISABLED;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.MAX_AGE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.VALUE;

public class WebappLogger extends ProcessEngineLogger {

  public static final String PROJECT_CODE = "WEBAPP";
  public static final String PROJECT_LOGGER = "org.camunda.bpm.webapp";

  public static final WebappLogger WEBAPP_LOGGER = BaseLogger.createLogger(
    WebappLogger.class, PROJECT_CODE, PROJECT_LOGGER, "01");

  public ProcessEngineException exceptionParametersCannotBeSet(String className) {
    return new ProcessEngineException(exceptionMessage(
      "001", className + ": cannot set " + VALUE.getName() + " in conjunction with " +
        MAX_AGE.getName() + " or " + INCLUDE_SUBDOMAINS_DISABLED.getName()) + ".");
  }

}
