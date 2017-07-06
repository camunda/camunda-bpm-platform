package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;

public class SetRetriesForExternalTasksDto {

  protected List<String> externalTaskIds;
  protected List<String> processInstanceIds;

  protected ExternalTaskQueryDto externalTaskQuery;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;

  protected int retries;

  public List<String> getExternalTaskIds() {
    return externalTaskIds;
  }

  public void setExternalTaskIds(List<String> externalTaskIds) {
    this.externalTaskIds = externalTaskIds;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public ExternalTaskQueryDto getExternalTaskQuery() {
    return externalTaskQuery;
  }

  public void setExternalTaskQuery(ExternalTaskQueryDto externalTaskQuery) {
    this.externalTaskQuery = externalTaskQuery;
  }

  public ProcessInstanceQueryDto getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQueryDto processInstanceQueryDto) {
    this.processInstanceQuery = processInstanceQueryDto;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto) {
    this.historicProcessInstanceQuery = historicProcessInstanceQueryDto;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }
}
