package org.camunda.bpm.engine.rest.dto.runtime.batch;

import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;

import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesDto {
  protected List<String> processInstanceIds;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected String deletionReason;


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

  public String getDeletionReason() {
    return deletionReason;
  }

  public void setDeletionReason(String deletionReason) {
    this.deletionReason = deletionReason;
  }
}
