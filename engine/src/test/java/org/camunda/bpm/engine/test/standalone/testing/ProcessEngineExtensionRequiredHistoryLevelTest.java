package org.camunda.bpm.engine.test.standalone.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineExtension;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProcessEngineExtensionRequiredHistoryLevelTest {
  
  @RegisterExtension
  ProcessEngineExtension extension = ProvidedProcessEngineExtension.builder()
      .configurationResource("org/camunda/bpm/engine/test/standalone/history/audithistory.camunda.cfg.xml")
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
