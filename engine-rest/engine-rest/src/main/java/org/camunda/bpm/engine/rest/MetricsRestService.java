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
package org.camunda.bpm.engine.rest;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsIntervalResultDto;

import org.camunda.bpm.engine.rest.sub.metrics.MetricsResource;
import org.camunda.bpm.engine.rest.util.DateParam;

/**
 * @author Daniel Meyer
 *
 */
@Produces(MediaType.APPLICATION_JSON)
public interface MetricsRestService {

  public static final String PATH = "/metrics";

  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{name}")
  MetricsResource getMetrics(@PathParam("name") String name);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<MetricsIntervalResultDto> interval(@QueryParam(value = "name") String name,
                        @QueryParam(value = "reporter") String reporter,
                        @QueryParam(value = "startDate") DateParam startDate,
                        @QueryParam(value = "endDate") DateParam endDate,
                        @QueryParam(value = "firstResult") Integer firstResult,
                        @QueryParam(value = "maxResults") Integer maxResults,
                        @QueryParam(value = "interval") Long interval);
}
