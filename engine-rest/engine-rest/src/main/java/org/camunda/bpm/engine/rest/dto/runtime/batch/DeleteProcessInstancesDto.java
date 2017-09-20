package org.camunda.bpm.engine.rest.dto.runtime.batch;

import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;

import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesDto {
  protected List<String> processInstanceIds;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected String deleteReason;
  protected boolean skipCustomListeners;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;
  protected boolean skipSubprocesses;

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public ProcessInstanceQueryDto getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public boolean isSkipSubprocesses() {
    return skipSubprocesses;
  }

  public void setSkipSubprocesses(boolean skipSubprocesses) {
    this.skipSubprocesses = skipSubprocesses;
  }

}
