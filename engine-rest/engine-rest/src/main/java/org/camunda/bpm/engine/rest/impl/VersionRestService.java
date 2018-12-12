/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.VersionDto;

import com.fasterxml.jackson.databind.ObjectMapper;

@Produces(MediaType.APPLICATION_JSON)
public class VersionRestService extends AbstractRestProcessEngineAware {

  public static final String PATH = "/version";

  public VersionRestService(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public VersionDto getVersion() {
    return new VersionDto(VersionRestService.class.getPackage().getImplementationVersion());
  }

}
