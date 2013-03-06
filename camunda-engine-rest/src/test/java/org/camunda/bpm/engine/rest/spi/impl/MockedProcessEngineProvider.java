package org.camunda.bpm.engine.rest.spi.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class MockedProcessEngineProvider implements ProcessEngineProvider {

  private static ProcessEngine cachedProcessEngine;
  
  @Override
  public synchronized ProcessEngine getProcessEngine() {
    if (cachedProcessEngine == null) {
      cachedProcessEngine = mock(ProcessEngine.class);
      mockServices(cachedProcessEngine);
    }
    
    return cachedProcessEngine;
  }
  
  private void mockServices(ProcessEngine engine) {
    RepositoryService repoService = mock(RepositoryService.class);
    IdentityService identityService = mock(IdentityService.class);
    TaskService taskService = mock(TaskService.class);
    RuntimeService runtimeService = mock(RuntimeService.class);
    FormService formService = mock(FormService.class);
    HistoryService historyService = mock(HistoryService.class);
    ManagementService managementService = mock(ManagementService.class);
    
    when(engine.getRepositoryService()).thenReturn(repoService);
    when(engine.getIdentityService()).thenReturn(identityService);
    when(engine.getTaskService()).thenReturn(taskService);
    when(engine.getRuntimeService()).thenReturn(runtimeService);
    when(engine.getFormService()).thenReturn(formService);
    when(engine.getHistoryService()).thenReturn(historyService);
    when(engine.getManagementService()).thenReturn(managementService);
  }

}
