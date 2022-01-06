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
package org.camunda.bpm.engine.rest.application;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.rest.exception.RestException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Does not declare produced media types.
 * @author Thorben Lindhauer
 *
 */
@Path("/unannotated")
public class UnannotatedResource {

  @GET
  @Path("/exception")
  public String throwAnException() throws Exception {
    throw new Exception("expected exception");
  }

  @GET
  @Path("/processEngineException")
  public String throwProcessEngineException() throws Exception {
    throw new ProcessEngineException("expected exception");
  }

  @GET
  @Path("/restException")
  public String throwRestException() throws Exception {
    throw new RestException(Status.BAD_REQUEST, "expected exception");
  }

  @GET
  @Path("/authorizationException")
  public String throwAuthorizationException() throws Exception {
    throw new AuthorizationException("someUser", "somePermission", "someResourceName", "someResourceId");
  }

  @GET
  @Path("/stackOverflowError")
  public String throwStackOverflowError() throws Throwable {
    throw new StackOverflowError("Stack overflow");
  }

  @GET
  @Path("/authorizationExceptionMultiple")
  public String throwAuthorizationExceptionMultiple() throws Exception {
    List<MissingAuthorization> missingAuthorizations = new ArrayList<>();

    missingAuthorizations.add(
        new MissingAuthorization("somePermission1", "someResourceName1", "someResourceId1"));
    missingAuthorizations.add(
        new MissingAuthorization("somePermission2", "someResourceName2", "someResourceId2"));
    throw new AuthorizationException("someUser", missingAuthorizations);
  }
}
