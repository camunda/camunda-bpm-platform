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
package org.camunda.bpm.admin.impl.plugin.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.admin.resource.AbstractAdminPluginRootResource;
import org.camunda.bpm.engine.rest.util.ProvidersUtil;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

@Path("plugin/base")
public class BaseRootResource extends AbstractAdminPluginRootResource {

  @Context
  protected Providers providers;

  public BaseRootResource() {
    super("base");
  }

  @Path("{engine}" + MetricsRestService.PATH)
  public MetricsRestService getProcessDefinitionResource(@PathParam("engine") String engineName) {
    MetricsRestService metricsRestService = new MetricsRestService(engineName);
    metricsRestService.setObjectMapper(getObjectMapper());
    return subResource(metricsRestService, engineName);
  }

  protected ObjectMapper getObjectMapper() {
    if (providers != null) {
      return ProvidersUtil
        .resolveFromContext(providers, ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE, this.getClass());
    } else {
      return null;
    }
  }

}
