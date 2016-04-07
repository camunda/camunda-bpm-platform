package org.camunda.bpm.engine.rest.impl.history;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricIdentityLink;
import org.camunda.bpm.engine.history.HistoricIdentityLinkQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricIdentityLinkRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkRestServiceImpl implements HistoricIdentityLinkRestService{

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricIdentityLinkRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricIdentityLinkDto> getHistoricIdentityLinks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricIdentityLinkQueryDto queryDto = new HistoricIdentityLinkQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkQuery query = queryDto.toQuery(processEngine);

    List<HistoricIdentityLink> queryResult;
    if (firstResult != null || maxResults != null) {
      queryResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      queryResult = query.list();
    }
    List<HistoricIdentityLinkDto> result = new ArrayList<HistoricIdentityLinkDto>();
    for (HistoricIdentityLink historicIdentityLink : queryResult) {
      HistoricIdentityLinkDto dto = HistoricIdentityLinkDto.fromHistoricIdentityLink(historicIdentityLink);
      result.add(dto);
    }
    return result;
  }

  @Override
  public CountResultDto getHistoricIdentityLinksCount(UriInfo uriInfo) {
    HistoricIdentityLinkQueryDto queryDto = new HistoricIdentityLinkQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;

  }
  private List<HistoricIdentityLink> executePaginatedQuery(HistoricIdentityLinkQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

}
