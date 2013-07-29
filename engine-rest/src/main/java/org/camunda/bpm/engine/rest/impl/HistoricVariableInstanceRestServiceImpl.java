package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceQueryDto;

public class HistoricVariableInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements
		HistoricVariableInstanceRestService {

	@Override
	public List<HistoricVariableInstanceDto> getHistoricVariableInstances(
			UriInfo uriInfo, Integer firstResult, Integer maxResults) {
		HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(uriInfo.getQueryParameters());
		return queryHistoricVariableInstances(queryDto, firstResult, maxResults);
	}

	@Override
	public List<HistoricVariableInstanceDto> queryHistoricVariableInstances(
			HistoricVariableInstanceQueryDto queryDto, Integer firstResult,
			Integer maxResults) {
		ProcessEngine engine = getProcessEngine();
		HistoricVariableInstanceQuery query = queryDto.toQuery(engine);

		List<HistoricVariableInstance> matchingHistoricVariableInstances;
		if (firstResult != null || maxResults != null) {
			matchingHistoricVariableInstances = executePaginatedQuery(query, firstResult, maxResults);
		} else {
			matchingHistoricVariableInstances = query.list();
		}

		List<HistoricVariableInstanceDto> historicVariableInstanceDtoResults = new ArrayList<HistoricVariableInstanceDto>();
		for (HistoricVariableInstance historicVariableInstance : matchingHistoricVariableInstances) {
			HistoricVariableInstanceDto resultHistoricVariableInstance = HistoricVariableInstanceDto.fromHistoricVariableInstance(historicVariableInstance);
			historicVariableInstanceDtoResults.add(resultHistoricVariableInstance);
		}
		return historicVariableInstanceDtoResults;
	}
	
	private List<HistoricVariableInstance> executePaginatedQuery(HistoricVariableInstanceQuery query,
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
	public CountResultDto getHistoricVariableInstancesCount(UriInfo uriInfo) {
	   HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(uriInfo.getQueryParameters());
	   return queryHistoricVariableInstancesCount(queryDto);
	}

	@Override
	public CountResultDto queryHistoricVariableInstancesCount(HistoricVariableInstanceQueryDto queryDto) {
	   ProcessEngine engine = getProcessEngine();
	   HistoricVariableInstanceQuery query = queryDto.toQuery(engine);
	    
	   long count = query.count();
	   CountResultDto result = new CountResultDto();
	   result.setCount(count);
	    
	   return result;
	}
}
