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
package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

import org.camunda.commons.logging.BaseLogger;

public class ExceptionLogger extends BaseLogger {

  public static final String PROJECT_CODE = "ENGINE-REST";
  public static final String REST_API = "org.camunda.bpm.engine.rest.exception";
  public static final ExceptionLogger REST_LOGGER = BaseLogger.createLogger(ExceptionLogger.class,
                                                                               PROJECT_CODE,
                                                                               REST_API,
                                                                               "HTTP");

  public void log(Throwable throwable) {
    Response.Status status = ExceptionHandlerHelper.getInstance().getStatus(throwable);
    int statusCode = status.getStatusCode();

    if ( statusCode >= 500) {
      logWarn(String.valueOf(statusCode), getStackTrace(throwable));
    } else {
      logDebug(String.valueOf(statusCode), getStackTrace(throwable));
    }
  }

  protected String getStackTrace(Throwable aThrowable) {

    final Writer      result      = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);

    return result.toString();
  }
}