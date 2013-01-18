package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

public class ProcessInstanceQueryDto extends AbstractQueryParameterDto {

  private String processDefinitionKey;

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  
  public ProcessInstanceQuery toQuery(RuntimeService runtimeService) {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    
    return query;
  }
  
  @Override
  public void setPropertyFromParameterPair(String key, String value) {
    try {
//      if (key.equals("active") || key.equals("suspended") || key.equals("latest")) {
//        Boolean booleanValue = new Boolean(value);
//        setValueBasedOnAnnotation(key, booleanValue);
//      } else if (key.equals("ver")) {
//        Integer intValue = new Integer(value);
//        setValueBasedOnAnnotation(key, intValue);
//      } else {
      setValueBasedOnAnnotation(key, value);
//      }
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Cannot set parameter.");
    } catch (IllegalAccessException e) {
      throw new RestException("Cannot set parameter.");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(e.getTargetException().getMessage());
    }
  }
}
