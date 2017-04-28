package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;

import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class SetJobRetriesByProcessDto {
  protected List<String> processInstances;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;

  protected Integer retries;

  public Integer getRetries() {
    return retries;
  }

  public void setRetries(Integer retries) {
    this.retries = retries;
  }

  public List<String> getProcessInstances() {
    return processInstances;
  }

  public void setProcessInstances(List<String> processInstances) {
    this.processInstances = processInstances;
  }

  public ProcessInstanceQueryDto getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }
}
