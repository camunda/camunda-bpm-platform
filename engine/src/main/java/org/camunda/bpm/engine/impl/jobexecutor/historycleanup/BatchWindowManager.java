package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author Svetlana Dorokhova.
 */
public interface BatchWindowManager {

  BatchWindow getCurrentOrNextBatchWindow(Date date, ProcessEngineConfigurationImpl configuration);

  BatchWindow getNextBatchWindow(Date date, ProcessEngineConfigurationImpl configuration);

  /**
   * When true, then for each date, it's possible to determine next batch window.
   * @param configuration
   * @return
   */
  boolean isBatchWindowConfigured(ProcessEngineConfigurationImpl configuration);
}
