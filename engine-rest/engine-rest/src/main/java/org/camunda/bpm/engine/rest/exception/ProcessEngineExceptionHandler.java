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
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Translates any {@link ProcessEngineException} to a HTTP 500 error and a JSON response.
 * Response content format: <code>{"type" : "ExceptionType", "message" : "some exception message"}</code>
 * </p>
 *
 * <p>Provides dedicated exception handling for {@link AuthorizationException AuthorizationExceptions}:
 * The status code is always set to 403, "Forbidden" and details about the requested resource and
 * violated permission are added to the response body</p>
 *
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 */
@Provider
public class ProcessEngineExceptionHandler implements ExceptionMapper<ProcessEngineException> {

  @Override
  public Response toResponse(ProcessEngineException exception) {
    return ExceptionHandlerHelper.getInstance().getResponse(exception);
  }
}
