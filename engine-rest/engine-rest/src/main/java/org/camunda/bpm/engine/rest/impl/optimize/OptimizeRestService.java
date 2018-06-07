/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl.optimize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableUpdateDto;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
public class OptimizeRestService extends AbstractRestProcessEngineAware {

  public static final String PATH = "/optimize";

  private DateConverter dateConverter;

  public OptimizeRestService(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
    dateConverter = new DateConverter();
    dateConverter.setObjectMapper(objectMapper);
  }

  @GET
  @Path("/activity-instance/completed")
  public List<HistoricActivityInstanceDto> getCompletedHistoricActivityInstances(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                                 @QueryParam("finishedAt") String finishedAtAsString,
                                                                                 @QueryParam("maxResults") int maxResults) {

    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<HistoricActivityInstance> historicActivityInstances =
      config.getOptimizeService().getCompletedHistoricActivityInstances(finishedAfter, finishedAt, maxResults);

    List<HistoricActivityInstanceDto> result = new ArrayList<HistoricActivityInstanceDto>();
    for (HistoricActivityInstance instance : historicActivityInstances) {
      HistoricActivityInstanceDto dto = HistoricActivityInstanceDto.fromHistoricActivityInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/process-instance/completed")
  public List<HistoricProcessInstanceDto> getCompletedHistoricProcessInstances(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                               @QueryParam("finishedAt") String finishedAtAsString,
                                                                               @QueryParam("maxResults") int maxResults) {
    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricProcessInstance> historicProcessInstances =
      config.getOptimizeService().getCompletedHistoricProcessInstances(finishedAfter, finishedAt, maxResults);

    List<HistoricProcessInstanceDto> result = new ArrayList<HistoricProcessInstanceDto>();
    for (HistoricProcessInstance instance : historicProcessInstances) {
      HistoricProcessInstanceDto dto = HistoricProcessInstanceDto.fromHistoricProcessInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/process-instance/running")
  public List<HistoricProcessInstanceDto> getRunningHistoricProcessInstances(@QueryParam("startedAfter") String startedAfterAsString,
                                                                             @QueryParam("startedAt") String startedAtAsString,
                                                                             @QueryParam("maxResults") int maxResults) {
    Date startedAfter = dateConverter.convertQueryParameterToType(startedAfterAsString);
    Date startedAt = dateConverter.convertQueryParameterToType(startedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricProcessInstance> historicProcessInstances =
      config.getOptimizeService().getRunningHistoricProcessInstances(startedAfter, startedAt, maxResults);

    List<HistoricProcessInstanceDto> result = new ArrayList<HistoricProcessInstanceDto>();
    for (HistoricProcessInstance instance : historicProcessInstances) {
      HistoricProcessInstanceDto dto = HistoricProcessInstanceDto.fromHistoricProcessInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/variable-update")
  public List<HistoricVariableUpdateDto> getHistoricVariableUpdates(@QueryParam("occurredAfter") String occurredAfterAsString,
                                                                    @QueryParam("occurredAt") String occurredAtAsString,
                                                                    @QueryParam("maxResults") int maxResults) {
    Date occurredAfter = dateConverter.convertQueryParameterToType(occurredAfterAsString);
    Date occurredAt = dateConverter.convertQueryParameterToType(occurredAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricVariableUpdate> historicVariableUpdates =
      config.getOptimizeService().getHistoricVariableUpdates(occurredAfter, occurredAt, maxResults);

    List<HistoricVariableUpdateDto> result = new ArrayList<HistoricVariableUpdateDto>();
    for (HistoricVariableUpdate instance : historicVariableUpdates) {
      HistoricVariableUpdateDto dto = HistoricVariableUpdateDto.fromHistoricVariableUpdate(instance);
      result.add(dto);
    }
    return result;
  }

  protected int ensureValidMaxResults(int givenMaxResults) {
    return givenMaxResults > 0 ? givenMaxResults : Integer.MAX_VALUE;
  }
}
