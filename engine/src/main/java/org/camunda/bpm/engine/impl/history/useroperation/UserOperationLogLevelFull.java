package org.camunda.bpm.engine.impl.history.useroperation;

import org.camunda.bpm.engine.ProcessEngineConfiguration;

public class UserOperationLogLevelFull extends AbstractUserOperationLogLevel {

  @Override
  public int getId() {
    return 10;
  }

  public String getName() {
    return ProcessEngineConfiguration.USER_OPERATION_LOG_FULL;
  }

  @Override
  public boolean isUserOperationLogEntryProduced(String eventType) {
    return true;
  }

}
