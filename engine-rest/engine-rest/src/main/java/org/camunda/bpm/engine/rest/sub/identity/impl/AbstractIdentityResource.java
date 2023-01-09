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
package org.camunda.bpm.engine.rest.sub.identity.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.AbstractAuthorizedRestResource;

import javax.ws.rs.core.Response.Status;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractIdentityResource extends AbstractAuthorizedRestResource {

  protected final IdentityService identityService;

  public AbstractIdentityResource(String processEngineName, Resource resource, String resourceId, ObjectMapper objectMapper) {
    super(processEngineName, resource, resourceId, objectMapper);
    this.identityService = getProcessEngine().getIdentityService();
  }

  protected void ensureNotReadOnly() {
    if(identityService.isReadOnly()) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Identity service implementation is read-only.");
    }
  }

}
