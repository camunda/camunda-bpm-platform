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
package org.camunda.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.ReportResult;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.converter.ReportResultToCsvConverter;
import org.camunda.bpm.engine.rest.dto.history.DeleteHistoricProcessInstancesDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceReportDto;
import org.camunda.bpm.engine.rest.dto.history.ReportResultDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricProcessInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricProcessInstanceResourceImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.List;

  public class HistoricProcessInstanceRestServiceImpl implements HistoricProcessInstanceRestService {

  public static final MediaType APPLICATION_CSV_TYPE = new MediaType("application", "csv");
  public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");
  public static final List<Variant> VARIANTS = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, APPLICATION_CSV_TYPE, TEXT_CSV_TYPE).add().build();

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricProcessInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricProcessInstanceResource getHistoricProcessInstance(String processInstanceId) {
    return new HistoricProcessInstanceResourceImpl(processEngine, processInstanceId);
  }

  @Override
  public List<HistoricProcessInstanceDto> getHistoricProcessInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricProcessInstanceQueryDto queryHistoriProcessInstanceDto = new HistoricProcessInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricProcessInstances(queryHistoriProcessInstanceDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricProcessInstanceDto> queryHistoricProcessInstances(HistoricProcessInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    queryDto.setObjectMapper(objectMapper);
    HistoricProcessInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricProcessInstance> matchingHistoricProcessInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricProcessInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricProcessInstances = query.list();
    }

    List<HistoricProcessInstanceDto> historicProcessInstanceDtoResults = new ArrayList<HistoricProcessInstanceDto>();
    for (HistoricProcessInstance historicProcessInstance : matchingHistoricProcessInstances) {
      HistoricProcessInstanceDto resultHistoricProcessInstanceDto = HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance);
      historicProcessInstanceDtoResults.add(resultHistoricProcessInstanceDto);
    }
    return historicProcessInstanceDtoResults;
  }

  private List<HistoricProcessInstance> executePaginatedQuery(HistoricProcessInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricProcessInstancesCount(UriInfo uriInfo) {
    HistoricProcessInstanceQueryDto queryDto = new HistoricProcessInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricProcessInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricProcessInstancesCount(HistoricProcessInstanceQueryDto queryDto) {
    queryDto.setObjectMapper(objectMapper);
    HistoricProcessInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @SuppressWarnings("unchecked")
  protected List<ReportResult> queryHistoricProcessInstanceReport(UriInfo uriInfo) {
    HistoricProcessInstanceReportDto reportDto = new HistoricProcessInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    return (List<ReportResult>) reportDto.executeReport(processEngine);
  }

  @Override
  public Response getHistoricProcessInstancesReport(UriInfo uriInfo, Request request) {
    Variant variant = request.selectVariant(VARIANTS);
    if (variant != null) {
      MediaType mediaType = variant.getMediaType();

      if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
        List<ReportResultDto> result = getReportResultAsJson(uriInfo);
        return Response.ok(result, mediaType).build();
      }
      else if (APPLICATION_CSV_TYPE.equals(mediaType) || TEXT_CSV_TYPE.equals(mediaType)) {
        String csv = getReportResultAsCsv(uriInfo);
        return Response
            .ok(csv, mediaType)
            .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
            .build();
      }
    }
    throw new InvalidRequestException(Status.NOT_ACCEPTABLE, "No acceptable content-type found");
  }

  @Override
  public BatchDto deleteAsync(DeleteHistoricProcessInstancesDto dto) {
    HistoryService historyService = processEngine.getHistoryService();

    HistoricProcessInstanceQuery historicProcessInstanceQuery = null;
    if (dto.getHistoricProcessInstanceQuery() != null) {
      historicProcessInstanceQuery = dto.getHistoricProcessInstanceQuery().toQuery(processEngine);
    }

    try {
      Batch batch = historyService.deleteHistoricProcessInstancesAsync(
          dto.getHistoricProcessInstanceIds(),
          historicProcessInstanceQuery,
          dto.getDeleteReason());
      return BatchDto.fromBatch(batch);

    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  protected List<ReportResultDto> getReportResultAsJson(UriInfo uriInfo) {
    List<ReportResult> reports = queryHistoricProcessInstanceReport(uriInfo);
    List<ReportResultDto> result = new ArrayList<ReportResultDto>();
    for (ReportResult report : reports) {
      result.add(ReportResultDto.fromReportResult(report));
    }
    return result;
  }

  protected String getReportResultAsCsv(UriInfo uriInfo) {
    List<ReportResult> reports = queryHistoricProcessInstanceReport(uriInfo);
    MultivaluedMap<String,String> queryParameters = uriInfo.getQueryParameters();
    String reportType = queryParameters.getFirst("reportType");
    return ReportResultToCsvConverter.convertReportResult(reports, reportType);
  }
}
