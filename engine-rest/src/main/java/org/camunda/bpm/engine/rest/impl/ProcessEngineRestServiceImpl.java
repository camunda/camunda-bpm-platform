package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.ProcessEngineRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionService;
import org.camunda.bpm.engine.rest.ProcessInstanceService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.ProcessEngineDto;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class ProcessEngineRestServiceImpl implements ProcessEngineRestService {

  @Override
  public ProcessDefinitionService getProcessDefinitionService(String engineName) {
    return new ProcessDefinitionServiceImpl(engineName);
  }

  @Override
  public ProcessInstanceService getProcessInstanceService(String engineName) {
    return new ProcessInstanceServiceImpl(engineName);
  }

  @Override
  public TaskRestService getTaskRestService(String engineName) {
    return new TaskRestServiceImpl(engineName);
  }

  @Override
  public List<ProcessEngineDto> getProcessEngineNames() {
    ProcessEngineProvider provider = getProcessEngineProvider();
    Set<String> engineNames = provider.getProcessEngineNames();
    
    List<ProcessEngineDto> results = new ArrayList<ProcessEngineDto>();
    for (String engineName : engineNames) {
      ProcessEngineDto dto = new ProcessEngineDto();
      dto.setName(engineName);
      results.add(dto);
    }
    
    return results;
  }
  
  
  private ProcessEngineProvider getProcessEngineProvider() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider;
    } else {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
}
