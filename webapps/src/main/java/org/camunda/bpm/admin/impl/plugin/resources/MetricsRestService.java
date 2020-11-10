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
import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.resource.AbstractAdminPluginResource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsResultDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Produces(MediaType.APPLICATION_JSON)
public class MetricsRestService extends AbstractAdminPluginResource {

  public static final String PATH = "/metrics";

  public static final String QUERY_PARAM_START_DATE = "startDate";
  public static final String QUERY_PARAM_END_DATE = "endDate";

  protected ObjectMapper objectMapper;

  public MetricsRestService(String engineName) {
    super(engineName);
  }

  @GET
  @Path("/task-worker/sum")
  @Produces(MediaType.APPLICATION_JSON)
  public MetricsResultDto countUniqueTaskWorkers(@QueryParam(QUERY_PARAM_START_DATE) String startDateAsString,
                                                 @QueryParam(QUERY_PARAM_END_DATE) String endDateAsString) {
    ProcessEngine processEngine = Admin.getRuntimeDelegate().getProcessEngine(engineName);
    ProcessEngineConfigurationImpl engineConfig =
      ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration());
    return engineConfig.getCommandExecutorTxRequired().execute(commandContext -> {
      // analogous to metrics, no permission check is performed

      DateConverter dateConverter = new DateConverter();
      dateConverter.setObjectMapper(objectMapper);

      Date startDate = convertToDate(dateConverter, startDateAsString);
      Date endDate = convertToDate(dateConverter, endDateAsString);

      MetricsResultDto result = new MetricsResultDto();
      long count = commandContext.getHistoricTaskInstanceManager()
        .findUniqueTaskWorkerCount(startDate, endDate);

      result.setResult(count);

      return result;
    });
  }

  protected Date convertToDate(DateConverter dateConverter,
                               String dateAsString) {
    return dateConverter.convertQueryParameterToType(dateAsString);
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

}
