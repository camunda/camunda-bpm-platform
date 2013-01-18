package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.rest.ProcessInstanceService;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceQueryDto;

public class ProcessInstanceServiceImpl extends AbstractEngineService implements
    ProcessInstanceService {

  @Override
  public List<ProcessInstanceDto> getProcessDefinitions(
      ProcessInstanceQueryDto queryDto) {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstanceQuery query = queryDto.toQuery(runtimeService);
    List<ProcessInstance> matchingInstances = query.list();
    
    List<ProcessInstanceDto> instanceResults = new ArrayList<ProcessInstanceDto>();
    for (ProcessInstance instance : matchingInstances) {
      ProcessInstanceDto resultInstance = ProcessInstanceDto.fromProcessInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }


}
