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
package org.camunda.bpm.engine.rest.sub.task.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.converter.TaskReportResultToCsvConverter;
import org.camunda.bpm.engine.rest.dto.task.TaskCountByCandidateGroupResultDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.task.TaskReportResource;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.List;

public class TaskReportResourceImpl implements TaskReportResource {

  public static final MediaType APPLICATION_CSV_TYPE = new MediaType("application", "csv");
  public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");
  public static final List<Variant> VARIANTS = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, APPLICATION_CSV_TYPE, TEXT_CSV_TYPE).add().build();

  protected ProcessEngine engine;

  public TaskReportResourceImpl(ProcessEngine engine) {
    this.engine = engine;
  }

  public Response getTaskCountByCandidateGroupReport(Request request) {
    Variant variant = request.selectVariant(VARIANTS);
    if (variant != null) {
      MediaType mediaType = variant.getMediaType();

      if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
        List<TaskCountByCandidateGroupResultDto> result = getTaskCountByCandidateGroupResultAsJson();
        return Response.ok(result, mediaType).build();
      }
      else if (APPLICATION_CSV_TYPE.equals(mediaType) || TEXT_CSV_TYPE.equals(mediaType)) {
        String csv = getReportResultAsCsv();
        return Response
          .ok(csv, mediaType)
          .header("Content-Disposition", "attachment; filename=task-count-by-candidate-group.csv")
          .build();
      }
    }
    throw new InvalidRequestException(Status.NOT_ACCEPTABLE, "No acceptable content-type found");
  }

  @SuppressWarnings("unchecked")
  protected List<TaskCountByCandidateGroupResult> queryTaskCountByCandidateGroupReport() {
    TaskCountByCandidateGroupResultDto reportDto = new TaskCountByCandidateGroupResultDto();
    return (List<TaskCountByCandidateGroupResult>) reportDto.executeTaskCountByCandidateGroupReport(engine);
  }

  protected List<TaskCountByCandidateGroupResultDto> getTaskCountByCandidateGroupResultAsJson() {
    List<TaskCountByCandidateGroupResult> reports = queryTaskCountByCandidateGroupReport();
    List<TaskCountByCandidateGroupResultDto> result = new ArrayList<TaskCountByCandidateGroupResultDto>();
    for (TaskCountByCandidateGroupResult report : reports) {
      result.add(TaskCountByCandidateGroupResultDto.fromTaskCountByCandidateGroupResultDto(report));
    }
    return result;
  }

  protected String getReportResultAsCsv() {
    List<TaskCountByCandidateGroupResult> reports = queryTaskCountByCandidateGroupReport();
    return TaskReportResultToCsvConverter.convertCandidateGroupReportResult(reports);
  }
}
