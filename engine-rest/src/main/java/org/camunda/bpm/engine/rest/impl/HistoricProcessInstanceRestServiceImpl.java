package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.rest.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;

public class HistoricProcessInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements
		HistoricProcessInstanceRestService {
	
	public HistoricProcessInstanceRestServiceImpl() {
		    super();
		  }
		  
	public HistoricProcessInstanceRestServiceImpl(String engineName) {
		    super(engineName);
	}
		  
	@Override
	public List<HistoricProcessInstanceDto> getHistoricProcessInstances(
			UriInfo uriInfo, Integer firstResult, Integer maxResults) {
		HistoricProcessInstanceQueryDto queryHistoriProcessInstanceDto = new HistoricProcessInstanceQueryDto(uriInfo.getQueryParameters());
		return queryHistoricProcessInstances(queryHistoriProcessInstanceDto, firstResult, maxResults);		
	}
	
	@Override
	public List<HistoricProcessInstanceDto> queryHistoricProcessInstances(HistoricProcessInstanceQueryDto queryDto, Integer firstResult,Integer maxResults) {
			
		ProcessEngine engine = getProcessEngine();
		HistoricProcessInstanceQuery query = queryDto.toQuery(engine);

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
	
	private List<HistoricProcessInstance> executePaginatedQuery(HistoricProcessInstanceQuery query,
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
	public CountResultDto getHistoricProcessInstancesCount(UriInfo uriInfo) {
	   HistoricProcessInstanceQueryDto queryDto = new HistoricProcessInstanceQueryDto(uriInfo.getQueryParameters());
	   return queryHistoricProcessInstancesCount(queryDto);
	}

	@Override
	public CountResultDto queryHistoricProcessInstancesCount(HistoricProcessInstanceQueryDto queryDto) {
	   ProcessEngine engine = getProcessEngine();
	   HistoricProcessInstanceQuery query = queryDto.toQuery(engine);
	    
	   long count = query.count();
	   CountResultDto result = new CountResultDto();
	   result.setCount(count);
	    
	   return result;
	}
}
