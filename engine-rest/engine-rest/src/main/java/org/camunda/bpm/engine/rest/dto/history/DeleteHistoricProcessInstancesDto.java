package org.camunda.bpm.engine.rest.dto.history;

import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstancesDto {

  protected List<String> historicProcessInstanceIds;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;
  protected String deleteReason;

  public List<String> getHistoricProcessInstanceIds() {
    return historicProcessInstanceIds;
  }

  public void setHistoricProcessInstanceIds(List<String> historicProcessInstanceIds) {
    this.historicProcessInstanceIds = historicProcessInstanceIds;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
}
