package org.camunda.bpm.engine.rest.impl.history;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkLogDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkLogQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricIdentityLinkLogRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogRestServiceImpl implements HistoricIdentityLinkLogRestService{

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricIdentityLinkLogRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricIdentityLinkLogDto> getHistoricIdentityLinks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricIdentityLinkLogQueryDto queryDto = new HistoricIdentityLinkLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkLogQuery query = queryDto.toQuery(processEngine);

    List<HistoricIdentityLinkLog> queryResult;
    if (firstResult != null || maxResults != null) {
      queryResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      queryResult = query.list();
    }
    List<HistoricIdentityLinkLogDto> result = new ArrayList<HistoricIdentityLinkLogDto>();
    for (HistoricIdentityLinkLog historicIdentityLink : queryResult) {
      HistoricIdentityLinkLogDto dto = HistoricIdentityLinkLogDto.fromHistoricIdentityLink(historicIdentityLink);
      result.add(dto);
    }
    return result;
  }

  @Override
  public CountResultDto getHistoricIdentityLinksCount(UriInfo uriInfo) {
    HistoricIdentityLinkLogQueryDto queryDto = new HistoricIdentityLinkLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkLogQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;

  }
  private List<HistoricIdentityLinkLog> executePaginatedQuery(HistoricIdentityLinkLogQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

}
