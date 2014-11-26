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
package org.camunda.bpm.engine.rest.history;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public interface HistoryRestService {

  public static final String PATH = "/history";

  @Path(HistoricProcessInstanceRestService.PATH)
  HistoricProcessInstanceRestService getProcessInstanceService();

  @Path(HistoricCaseInstanceRestService.PATH)
  HistoricCaseInstanceRestService getCaseInstanceService();

  @Path(HistoricActivityInstanceRestService.PATH)
  HistoricActivityInstanceRestService getActivityInstanceService();

  @Path(HistoricCaseActivityInstanceRestService.PATH)
  HistoricCaseActivityInstanceRestService getCaseActivityInstanceService();

  @Path(HistoricVariableInstanceRestService.PATH)
  HistoricVariableInstanceRestService getVariableInstanceService();

  @Path(HistoricActivityStatisticsRestService.PATH)
  HistoricActivityStatisticsRestService getActivityStatisticsService();

  @Path(UserOperationLogRestService.PATH)
  UserOperationLogRestService getUserOperationLogRestService();

  @Path(HistoricDetailRestService.PATH)
  HistoricDetailRestService getDetailService();

  @Path(HistoricTaskInstanceRestService.PATH)
  HistoricTaskInstanceRestService getTaskInstanceService();

  @Path(HistoricIncidentRestService.PATH)
  HistoricIncidentRestService getIncidentService();

}
