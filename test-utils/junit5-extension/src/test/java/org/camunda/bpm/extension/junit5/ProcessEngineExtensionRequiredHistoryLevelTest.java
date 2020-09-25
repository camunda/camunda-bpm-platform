package org.camunda.bpm.extension.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProcessEngineExtensionRequiredHistoryLevelTest {
  
  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder()
      .configurationResource("audithistory.camunda.cfg.xml")
      .build();

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testRequiredHistoryIgnored() {
    fail("the configured history level is too high");
  }
  
  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testRequiredHistoryLevelMatch() {
    assertEquals(extension.getProcessEngineConfiguration().getHistoryLevel().getName(), 
        ProcessEngineConfiguration.HISTORY_AUDIT);
  }
}
