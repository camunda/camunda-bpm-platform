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

import org.camunda.bpm.engine.rest.dto.history.HistoricActivityStatisticsDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
*
* @author Roman Smirnov
*
*/
@Path(HistoricActivityStatisticsRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricActivityStatisticsRestService {

  public static final String PATH = "/process-definition/{id}/statistics";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<HistoricActivityStatisticsDto> getHistoricActivityStatistics(@PathParam("id") String processDefinitionId, @QueryParam("canceled") Boolean includeCanceled,
      @QueryParam("finished") Boolean includeFinished, @QueryParam("completeScope") Boolean includeCompleteScope,
      @QueryParam("sortBy") String sortBy, @QueryParam("sortOrder") String sortOrder);

}
