package org.camunda.bpm.cycle.web.dto;


public class SynchronizationResultDTO {
  
  private SynchronizationStatus status;
  
  private String message;
  
  public SynchronizationResultDTO() { }
  
  public SynchronizationResultDTO(SynchronizationStatus status) {
    this.status = status;
  }
  
  public SynchronizationResultDTO(SynchronizationStatus status, String message) {
    this.status = status;
    this.message = message;
  }
  
  public SynchronizationStatus getStatus() {
    return status;
  }
  
  public String getMessage() {
    return message;
  }

  public enum SynchronizationStatus {
    SYNC_SUCCESS,
    SYNC_FAILED
  }
  
}
