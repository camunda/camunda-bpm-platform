package org.camunda.bpm.cockpit.impl.plugin.base.dto.query;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

public class ProcessDefinitionQueryDto extends AbstractRestQueryParametersDto<ProcessDefinitionDto> {

  private static final long serialVersionUID = 1L;
  
  protected String parentProcessDefinitionId;
  protected String superProcessDefinitionId;
  protected String[] activityIdIn;
  
  public ProcessDefinitionQueryDto() { }
  
  public ProcessDefinitionQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  public String getParentProcessDefinitionId() {
    return parentProcessDefinitionId;
  }
  
  @CamundaQueryParam(value="parentProcessDefinitionId")
  public void setParentProcessDefinitionId(String parentProcessDefinitionId) {
    this.parentProcessDefinitionId = parentProcessDefinitionId;
  }
  
  public String getSuperProcessDefinitionId() {
    return superProcessDefinitionId;
  }
  
  @CamundaQueryParam(value="superProcessDefinitionId")
  public void setSuperProcessDefinitionId(String superProcessDefinitionId) {
    this.superProcessDefinitionId = superProcessDefinitionId;
  }

  public String[] getActivityIdIn() {
    return activityIdIn;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  @Override
  protected String getOrderByValue(String sortBy) {
    return super.getOrderBy();
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

}
