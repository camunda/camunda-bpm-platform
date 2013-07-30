package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.HistoricActivityInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceQueryDto;

public class HistoricActivityInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements
		HistoricActivityInstanceRestService {

	@Override
	public List<HistoricActivityInstanceDto> getHistoricActivityInstances(
			UriInfo uriInfo, Integer firstResult, Integer maxResults) {
		HistoricActivityInstanceQueryDto queryHistoricActivityInstanceDto = new HistoricActivityInstanceQueryDto(uriInfo.getQueryParameters());
		return queryHistoricActivityInstances(queryHistoricActivityInstanceDto, firstResult, maxResults);		
	}

	@Override
	public List<HistoricActivityInstanceDto> queryHistoricActivityInstances(
			HistoricActivityInstanceQueryDto queryDto, Integer firstResult,
			Integer maxResults) {
		ProcessEngine engine = getProcessEngine();
		HistoricActivityInstanceQuery query = queryDto.toQuery(engine);

		List<HistoricActivityInstance> matchingHistoricActivityInstances;
		if (firstResult != null || maxResults != null) {
			matchingHistoricActivityInstances = executePaginatedQuery(query, firstResult, maxResults);
		} else {
			matchingHistoricActivityInstances = query.list();
		}

		List<HistoricActivityInstanceDto> historicActivityInstanceResults = new ArrayList<HistoricActivityInstanceDto>();
		for (HistoricActivityInstance historicActivityInstance : matchingHistoricActivityInstances) {
			HistoricActivityInstanceDto resultHistoricActivityInstance = HistoricActivityInstanceDto.fromHistoricActivityInstance(historicActivityInstance);
			historicActivityInstanceResults.add(resultHistoricActivityInstance);
		}
		return historicActivityInstanceResults;
	}
	
	private List<HistoricActivityInstance> executePaginatedQuery(HistoricActivityInstanceQuery query,
			Integer firstResult, Integer maxResults) {
		if (firstResult == null) {
			firstResult = 0;
		}
		if (maxResults == null) {
			maxResults = Integer.MAX_VALUE;
		}
		return query.listPage(firstResult, maxResults);
	}
	
	@Override
	public CountResultDto getHistoricActivityInstancesCount(UriInfo uriInfo) {
	   HistoricActivityInstanceQueryDto queryDto = new HistoricActivityInstanceQueryDto(uriInfo.getQueryParameters());
	   return queryHistoricActivityInstancesCount(queryDto);
	}

	@Override
	public CountResultDto queryHistoricActivityInstancesCount(HistoricActivityInstanceQueryDto queryDto) {
	   ProcessEngine engine = getProcessEngine();
	   HistoricActivityInstanceQuery query = queryDto.toQuery(engine);
	    
	   long count = query.count();
	   CountResultDto result = new CountResultDto();
	   result.setCount(count);
	    
	   return result;
	}
}
