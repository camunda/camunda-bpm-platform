package org.camunda.bpm.engine.impl.history.useroperation;

import org.camunda.bpm.engine.ProcessEngineConfiguration;

public class UserOperationLogLevelNone extends AbstractUserOperationLogLevel {

  @Override
  public int getId() {
    return 11;
  }

  public String getName() {
    return ProcessEngineConfiguration.USER_OPERATION_LOG_NONE;
  }

  @Override
  public boolean isUserOperationLogEntryProduced(String eventType) {
    return false;
  }


}
